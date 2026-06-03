package org.example.stationery_shop.service.serviceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.stationery_shop.dto.request.ProductVariantImageImportRequest;
import org.example.stationery_shop.dto.response.ProductVariantImageImportItemResponse;
import org.example.stationery_shop.dto.response.ProductVariantImageImportJobResponse;
import org.example.stationery_shop.entity.catalog.ProductVariant;
import org.example.stationery_shop.entity.catalog.ProductVariantImage;
import org.example.stationery_shop.enums.ImageImportItemStatus;
import org.example.stationery_shop.enums.ImageImportJobStatus;
import org.example.stationery_shop.exception.AppException;
import org.example.stationery_shop.exception.ErrorCode;
import org.example.stationery_shop.repository.ProductVariantImageRepository;
import org.example.stationery_shop.repository.ProductVariantRepository;
import org.example.stationery_shop.service.CloudinaryService;
import org.example.stationery_shop.service.ProductVariantImageImportService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductVariantImageImportServiceImpl implements ProductVariantImageImportService {
    private static final String JOB_KEY_PREFIX = "catalog:variant-image-import:job:";
    private static final String QUEUE_KEY = "catalog:variant-image-import:queue";
    private static final String CLOUDINARY_ROOT_FOLDER = "stationery-shop/product-variants";
    private static final Duration JOB_TTL = Duration.ofHours(24);
    private static final Pattern IMAGE_FILE_PATTERN = Pattern.compile(
            "image_(\\d+)\\.(jpg|jpeg|png|webp)$",
            Pattern.CASE_INSENSITIVE
    );

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final CloudinaryService cloudinaryService;
    private final ProductVariantRepository productVariantRepository;
    private final ProductVariantImageRepository productVariantImageRepository;
    private final TransactionTemplate transactionTemplate;

    private ExecutorService workerExecutor;
    private volatile boolean running;

    @PostConstruct
    void startWorker() {
        running = true;
        workerExecutor = Executors.newSingleThreadExecutor();
        workerExecutor.submit(this::consumeQueue);
    }

    @PreDestroy
    void stopWorker() {
        running = false;
        if (workerExecutor != null) {
            workerExecutor.shutdownNow();
            try {
                if (!workerExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.warn("Variant image import worker did not stop within timeout");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public ProductVariantImageImportJobResponse startImport(ProductVariantImageImportRequest request) {
        Path rootFolder = resolveRootFolder(request == null ? null : request.getRootFolder());
        List<ProductVariantImageImportItemResponse> items = scanImportItems(rootFolder);
        if (items.isEmpty()) {
            throw new AppException(ErrorCode.IMAGE_IMPORT_FOLDER_EMPTY);
        }

        ProductVariantImageImportJobResponse job = ProductVariantImageImportJobResponse.builder()
                .jobId(UUID.randomUUID().toString())
                .status(ImageImportJobStatus.PENDING)
                .rootFolder(rootFolder.toString())
                .totalItems(items.size())
                .pendingItems(items.size())
                .successItems(0)
                .failedItems(0)
                .progressPercent(0)
                .createdAt(Instant.now())
                .items(items)
                .build();

        saveJob(job);
        redisTemplate.opsForList().leftPush(QUEUE_KEY, job.getJobId());
        return job;
    }

    @Override
    public ProductVariantImageImportJobResponse getJob(String jobId) {
        ProductVariantImageImportJobResponse job = readJob(jobId);
        if (job == null) {
            throw new AppException(ErrorCode.IMAGE_IMPORT_JOB_NOT_FOUND);
        }
        return job;
    }

    private void consumeQueue() {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                Object value = redisTemplate.opsForList().rightPop(QUEUE_KEY, Duration.ofSeconds(5));
                if (value != null) {
                    processJob(value.toString());
                }
            } catch (Exception e) {
                if (!running || Thread.currentThread().isInterrupted()) {
                    Thread.currentThread().interrupt();
                    break;
                }
                log.error("Variant image import worker failed", e);
            }
        }
    }

    private void processJob(String jobId) {
        ProductVariantImageImportJobResponse job = readJob(jobId);
        if (job == null) {
            return;
        }

        job.setStatus(ImageImportJobStatus.RUNNING);
        job.setStartedAt(Instant.now());
        saveJob(job);

        try {
            for (int index = 0; index < job.getItems().size(); index++) {
                ProductVariantImageImportItemResponse item = job.getItems().get(index);
                item.setStatus(ImageImportItemStatus.UPLOADING);
                saveJob(job);
                try {
                    processItem(item);
                    item.setStatus(ImageImportItemStatus.SUCCESS);
                } catch (Exception e) {
                    item.setStatus(ImageImportItemStatus.FAILED);
                    item.setErrorMessage(e.getMessage());
                    log.warn("Variant image import item failed: variant={}, file={}",
                            item.getProductVariantId(),
                            item.getFileName());
                }
                refreshProgress(job);
                saveJob(job);
            }

            finishJob(job);
        } catch (Exception e) {
            job.setStatus(ImageImportJobStatus.FAILED);
            job.setErrorMessage(e.getMessage());
            job.setFinishedAt(Instant.now());
            refreshProgress(job);
            saveJob(job);
            log.error("Variant image import job {} failed", jobId, e);
        }
    }

    private void processItem(ProductVariantImageImportItemResponse item) {
        try {
            Path filePath = Path.of(item.getLocalFilePath());
            String imageUrl = cloudinaryService.uploadImage(
                    filePath,
                    item.getCloudinaryFolder(),
                    item.getCloudinaryPublicId()
            );
            transactionTemplate.executeWithoutResult(status -> saveVariantImage(item, imageUrl));
            item.setImageUrl(imageUrl);
        } catch (Exception e) {
            if (e instanceof AppException appException) {
                throw appException;
            }
            throw new AppException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }

    private void saveVariantImage(ProductVariantImageImportItemResponse item, String imageUrl) {
        ProductVariant variant = productVariantRepository.findById(item.getProductVariantId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXIST));

        if (item.isPrimaryImage()) {
            productVariantImageRepository.clearPrimaryImage(item.getProductVariantId());
        }

        ProductVariantImage image = productVariantImageRepository
                .findByProductVariantIdAndSortOrder(item.getProductVariantId(), item.getSortOrder())
                .orElseGet(ProductVariantImage::new);
        image.setProductVariant(variant);
        image.setImageUrl(imageUrl);
        image.setPrimaryImage(item.isPrimaryImage());
        image.setSortOrder(item.getSortOrder());
        productVariantImageRepository.save(image);
    }

    private Path resolveRootFolder(String rootFolder) {
        Path projectRoot = Path.of("").toAbsolutePath().normalize();
        Path resolved = rootFolder == null || rootFolder.isBlank()
                ? projectRoot.resolve("img")
                : Path.of(rootFolder);
        if (!resolved.isAbsolute()) {
            resolved = projectRoot.resolve(resolved);
        }
        resolved = resolved.normalize();
        if (!Files.isDirectory(resolved)) {
            throw new AppException(ErrorCode.IMAGE_IMPORT_FOLDER_INVALID);
        }
        return resolved;
    }

    private List<ProductVariantImageImportItemResponse> scanImportItems(Path rootFolder) {
        List<ProductVariantImageImportItemResponse> items = new ArrayList<>();
        try (var variantFolders = Files.list(rootFolder)) {
            variantFolders
                    .filter(Files::isDirectory)
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .forEach(variantFolder -> items.addAll(scanVariantFolder(variantFolder)));
        } catch (IOException e) {
            throw new AppException(ErrorCode.IMAGE_IMPORT_FOLDER_INVALID);
        }
        return items;
    }

    private List<ProductVariantImageImportItemResponse> scanVariantFolder(Path variantFolder) {
        String productVariantId = variantFolder.getFileName().toString();
        List<ProductVariantImageImportItemResponse> items = new ArrayList<>();
        try (var imageFiles = Files.list(variantFolder)) {
            imageFiles
                    .filter(Files::isRegularFile)
                    .map(this::toImportFile)
                    .filter(importFile -> importFile != null)
                    .sorted(Comparator.comparingInt(ImportFile::sortOrder))
                    .forEach(importFile -> items.add(toImportItem(productVariantId, importFile)));
        } catch (IOException e) {
            ProductVariantImageImportItemResponse failedItem = ProductVariantImageImportItemResponse.builder()
                    .productVariantId(productVariantId)
                    .localFilePath(variantFolder.toString())
                    .status(ImageImportItemStatus.FAILED)
                    .errorMessage(e.getMessage())
                    .build();
            items.add(failedItem);
        }
        return items;
    }

    private ImportFile toImportFile(Path filePath) {
        String fileName = filePath.getFileName().toString();
        Matcher matcher = IMAGE_FILE_PATTERN.matcher(fileName);
        if (!matcher.matches()) {
            return null;
        }
        int sortOrder = Integer.parseInt(matcher.group(1));
        String publicId = fileName.substring(0, fileName.lastIndexOf('.')).toLowerCase(Locale.ROOT);
        return new ImportFile(filePath, fileName, publicId, sortOrder);
    }

    private ProductVariantImageImportItemResponse toImportItem(String productVariantId, ImportFile importFile) {
        return ProductVariantImageImportItemResponse.builder()
                .productVariantId(productVariantId)
                .fileName(importFile.fileName())
                .localFilePath(importFile.filePath().toString())
                .sortOrder(importFile.sortOrder())
                .primaryImage(importFile.sortOrder() == 1)
                .cloudinaryFolder(CLOUDINARY_ROOT_FOLDER + "/" + productVariantId)
                .cloudinaryPublicId(importFile.publicId())
                .status(ImageImportItemStatus.PENDING)
                .build();
    }

    private void finishJob(ProductVariantImageImportJobResponse job) {
        job.setStatus(job.getFailedItems() == 0
                ? ImageImportJobStatus.COMPLETED
                : ImageImportJobStatus.COMPLETED_WITH_ERRORS);
        job.setFinishedAt(Instant.now());
        refreshProgress(job);
        saveJob(job);
    }

    private void refreshProgress(ProductVariantImageImportJobResponse job) {
        int failedItems = (int) job.getItems().stream()
                .filter(item -> item.getStatus() == ImageImportItemStatus.FAILED)
                .count();
        int successItems = (int) job.getItems().stream()
                .filter(item -> item.getStatus() == ImageImportItemStatus.SUCCESS)
                .count();
        job.setFailedItems(failedItems);
        job.setSuccessItems(successItems);
        job.setPendingItems(Math.max(job.getTotalItems() - successItems - failedItems, 0));
        job.setProgressPercent(job.getTotalItems() == 0
                ? 100
                : ((successItems + failedItems) * 100) / job.getTotalItems());
    }

    private void saveJob(ProductVariantImageImportJobResponse job) {
        redisTemplate.opsForValue().set(jobKey(job.getJobId()), job, JOB_TTL);
    }

    private ProductVariantImageImportJobResponse readJob(String jobId) {
        Object value = redisTemplate.opsForValue().get(jobKey(jobId));
        if (value == null) {
            return null;
        }
        return objectMapper.convertValue(value, ProductVariantImageImportJobResponse.class);
    }

    private String jobKey(String jobId) {
        return JOB_KEY_PREFIX + jobId;
    }

    private record ImportFile(Path filePath, String fileName, String publicId, int sortOrder) {
    }
}
