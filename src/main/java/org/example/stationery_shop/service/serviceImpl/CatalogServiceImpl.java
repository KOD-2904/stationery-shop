package org.example.stationery_shop.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.stationery_shop.dto.request.BrandRequest;
import org.example.stationery_shop.dto.request.CategoryRequest;
import org.example.stationery_shop.dto.request.ProductRequest;
import org.example.stationery_shop.dto.request.ProductVariantRequest;
import org.example.stationery_shop.dto.response.BrandResponse;
import org.example.stationery_shop.dto.response.CatalogImportResponse;
import org.example.stationery_shop.dto.response.CategoryResponse;
import org.example.stationery_shop.dto.response.ProductImageResponse;
import org.example.stationery_shop.dto.response.ProductResponse;
import org.example.stationery_shop.dto.response.ProductVariantImageResponse;
import org.example.stationery_shop.dto.response.ProductVariantResponse;
import org.example.stationery_shop.entity.catalog.Brand;
import org.example.stationery_shop.entity.catalog.Category;
import org.example.stationery_shop.entity.catalog.Product;
import org.example.stationery_shop.entity.catalog.ProductImage;
import org.example.stationery_shop.entity.catalog.ProductVariant;
import org.example.stationery_shop.entity.catalog.ProductVariantImage;
import org.example.stationery_shop.enums.PricingType;
import org.example.stationery_shop.exception.AppException;
import org.example.stationery_shop.exception.ErrorCode;
import org.example.stationery_shop.mapper.CatalogMapper;
import org.example.stationery_shop.repository.BrandRepository;
import org.example.stationery_shop.repository.CategoryRepository;
import org.example.stationery_shop.repository.ProductImageRepository;
import org.example.stationery_shop.repository.ProductRepository;
import org.example.stationery_shop.repository.ProductVariantImageRepository;
import org.example.stationery_shop.repository.ProductVariantRepository;
import org.example.stationery_shop.service.CatalogService;
import org.example.stationery_shop.service.CloudinaryService;
import org.example.stationery_shop.utils.SlugUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CatalogServiceImpl implements CatalogService {
    private static final String CLOUDINARY_ROOT_FOLDER = "Jewelry Shop";

    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductVariantImageRepository productVariantImageRepository;
    private final CatalogMapper catalogMapper;
    private final CloudinaryService cloudinaryService;

    @Override
    public List<BrandResponse> getBrands(boolean includeInactive) {
        List<Brand> brands = includeInactive
                ? brandRepository.findAll()
                : brandRepository.findByActiveTrueOrderByNameAsc();
        return brands.stream().map(catalogMapper::toBrandResponse).toList();
    }

    @Override
    @Transactional
    public BrandResponse createBrand(BrandRequest request) {
        String slug = resolveSlug(request.getSlug(), request.getName());
        ensureSlugAvailable(brandRepository.existsBySlug(slug));
        Brand brand = Brand.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .logoUrl(request.getLogoUrl())
                .active(request.getActive() == null || request.getActive())
                .build();
        return catalogMapper.toBrandResponse(brandRepository.save(brand));
    }

    @Override
    @Transactional
    public BrandResponse updateBrand(String id, BrandRequest request) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_EXIST));
        String slug = resolveSlug(request.getSlug(), request.getName());
        if (!brand.getSlug().equals(slug) && brandRepository.existsBySlug(slug)) {
            throw new AppException(ErrorCode.SLUG_EXISTED);
        }
        brand.setName(request.getName());
        brand.setSlug(slug);
        brand.setDescription(request.getDescription());
        brand.setLogoUrl(request.getLogoUrl());
        if (request.getActive() != null) {
            brand.setActive(request.getActive());
        }
        return catalogMapper.toBrandResponse(brandRepository.save(brand));
    }

    @Override
    @Transactional
    public BrandResponse uploadBrandLogo(String id, MultipartFile file) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_EXIST));
        brand.setLogoUrl(cloudinaryService.uploadImage(file, brandFolder()));
        return catalogMapper.toBrandResponse(brandRepository.save(brand));
    }

    @Override
    public List<CategoryResponse> getCategories(boolean includeInactive) {
        List<Category> categories = includeInactive
                ? categoryRepository.findAll()
                : categoryRepository.findByActiveTrueOrderByNameAsc();
        return categories.stream().map(catalogMapper::toCategoryResponse).toList();
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        String slug = resolveSlug(request.getSlug(), request.getName());
        ensureSlugAvailable(categoryRepository.existsBySlug(slug));
        Category category = Category.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .active(request.getActive() == null || request.getActive())
                .build();
        return catalogMapper.toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(String id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXIST));
        String slug = resolveSlug(request.getSlug(), request.getName());
        if (!category.getSlug().equals(slug) && categoryRepository.existsBySlug(slug)) {
            throw new AppException(ErrorCode.SLUG_EXISTED);
        }
        category.setName(request.getName());
        category.setSlug(slug);
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());
        if (request.getActive() != null) {
            category.setActive(request.getActive());
        }
        return catalogMapper.toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse uploadCategoryImage(String id, MultipartFile file) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXIST));
        category.setImageUrl(cloudinaryService.uploadImage(file, categoryFolder()));
        return catalogMapper.toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    public List<ProductResponse> getProducts(boolean includeInactive) {
        List<Product> products = includeInactive
                ? productRepository.findAllByOrderByCreatedAtDesc()
                : productRepository.findByActiveTrueOrderByCreatedAtDesc();
        return products.stream().map(catalogMapper::toProductResponse).toList();
    }

    @Override
    public ProductResponse getProduct(String id) {
        Product product = productRepository.findWithBrandAndCategoryById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXIST));
        return catalogMapper.toProductResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        String slug = resolveSlug(request.getSlug(), request.getName());
        ensureSlugAvailable(productRepository.existsBySlug(slug));
        Product product = Product.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .brand(resolveBrand(request.getBrandId()))
                .category(resolveCategory(request.getCategoryId()))
                .material(request.getMaterial())
                .goldAge(request.getGoldAge())
                .stoneType(request.getStoneType())
                .thumbnailUrl(request.getThumbnailUrl())
                .pricingType(request.getPricingType() == null ? PricingType.ABSOLUTE_FIXED : request.getPricingType())
                .active(request.getActive() == null || request.getActive())
                .build();
        Product savedProduct = productRepository.save(product);
        return getProduct(savedProduct.getId());
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(String id, ProductRequest request) {
        Product product = productRepository.findWithBrandAndCategoryById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXIST));
        String slug = resolveSlug(request.getSlug(), request.getName());
        if (!product.getSlug().equals(slug) && productRepository.existsBySlug(slug)) {
            throw new AppException(ErrorCode.SLUG_EXISTED);
        }
        product.setName(request.getName());
        product.setSlug(slug);
        product.setDescription(request.getDescription());
        product.setBrand(resolveBrand(request.getBrandId()));
        product.setCategory(resolveCategory(request.getCategoryId()));
        product.setMaterial(request.getMaterial());
        product.setGoldAge(request.getGoldAge());
        product.setStoneType(request.getStoneType());
        product.setThumbnailUrl(request.getThumbnailUrl());
        if (request.getPricingType() != null) {
            product.setPricingType(request.getPricingType());
        }
        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }
        Product savedProduct = productRepository.save(product);
        return getProduct(savedProduct.getId());
    }

    @Override
    @Transactional
    public ProductResponse uploadProductThumbnail(String id, MultipartFile file) {
        Product product = productRepository.findWithBrandAndCategoryById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXIST));
        product.setThumbnailUrl(cloudinaryService.uploadImage(file, productFolder(id)));
        productRepository.save(product);
        return getProduct(id);
    }

    @Override
    @Transactional
    public ProductImageResponse uploadProductImage(String productId, MultipartFile file, boolean primaryImage, Integer sortOrder) {
        Product product = productRepository.findWithBrandAndCategoryById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXIST));
        ProductImage image = ProductImage.builder()
                .product(product)
                .imageUrl(cloudinaryService.uploadImage(file, productFolder(productId)))
                .primaryImage(primaryImage)
                .sortOrder(sortOrder)
                .build();
        ProductImage savedImage = productImageRepository.save(image);
        if (primaryImage && product.getThumbnailUrl() == null) {
            product.setThumbnailUrl(savedImage.getImageUrl());
            productRepository.save(product);
        }
        return catalogMapper.toProductImageResponse(savedImage);
    }

    @Override
    @Transactional
    public ProductVariantResponse createVariant(String productId, ProductVariantRequest request) {
        if (productVariantRepository.existsBySku(request.getSku())) {
            throw new AppException(ErrorCode.SKU_EXISTED);
        }
        Product product = productRepository.findWithBrandAndCategoryById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXIST));
        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .sku(request.getSku())
                .size(request.getSize())
                .price(request.getPrice())
                .goldWeight(request.getGoldWeight())
                .laborCost(request.getLaborCost())
                .imageUrl(request.getImageUrl())
                .active(request.getActive() == null || request.getActive())
                .build();
        return catalogMapper.toProductVariantResponse(productVariantRepository.save(variant));
    }

    @Override
    @Transactional
    public ProductVariantResponse updateVariant(String id, ProductVariantRequest request) {
        ProductVariant variant = productVariantRepository.findWithProductById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXIST));
        if (!variant.getSku().equals(request.getSku()) && productVariantRepository.existsBySku(request.getSku())) {
            throw new AppException(ErrorCode.SKU_EXISTED);
        }
        variant.setSku(request.getSku());
        variant.setSize(request.getSize());
        variant.setPrice(request.getPrice());
        variant.setGoldWeight(request.getGoldWeight());
        variant.setLaborCost(request.getLaborCost());
        variant.setImageUrl(request.getImageUrl());
        if (request.getActive() != null) {
            variant.setActive(request.getActive());
        }
        return catalogMapper.toProductVariantResponse(productVariantRepository.save(variant));
    }

    @Override
    @Transactional
    public ProductVariantResponse uploadVariantMainImage(String id, MultipartFile file) {
        ProductVariant variant = productVariantRepository.findWithProductById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXIST));
        variant.setImageUrl(cloudinaryService.uploadImage(file, variantFolder(variant)));
        return catalogMapper.toProductVariantResponse(productVariantRepository.save(variant));
    }

    @Override
    @Transactional
    public ProductVariantImageResponse uploadVariantImage(String id, MultipartFile file, boolean primaryImage, Integer sortOrder) {
        ProductVariant variant = productVariantRepository.findWithProductById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXIST));
        ProductVariantImage image = ProductVariantImage.builder()
                .productVariant(variant)
                .imageUrl(cloudinaryService.uploadImage(file, variantFolder(variant)))
                .primaryImage(primaryImage)
                .sortOrder(sortOrder)
                .build();
        ProductVariantImage savedImage = productVariantImageRepository.save(image);
        if (primaryImage && variant.getImageUrl() == null) {
            variant.setImageUrl(savedImage.getImageUrl());
            productVariantRepository.save(variant);
        }
        return catalogMapper.toProductVariantImageResponse(savedImage);
    }

    @Override
    @Transactional
    public CatalogImportResponse importCatalog(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.CATALOG_IMPORT_FILE_INVALID);
        }

        CatalogImportStats stats = new CatalogImportStats();
        List<CatalogImportResponse.CatalogImportRowResult> rowResults = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getNumberOfSheets() == 0 ? null : workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() <= 1) {
                throw new AppException(ErrorCode.CATALOG_IMPORT_FILE_INVALID);
            }

            DataFormatter formatter = new DataFormatter(Locale.US);
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (isBlankRow(row, formatter)) {
                    continue;
                }

                stats.totalRows++;
                CatalogImportResponse.CatalogImportRowResult rowResult = importCatalogRow(row, formatter, rowIndex + 1, stats);
                rowResults.add(rowResult);
                if (rowResult.isSuccess()) {
                    stats.successRows++;
                } else {
                    stats.failedRows++;
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            throw new AppException(ErrorCode.CATALOG_IMPORT_FILE_INVALID);
        }

        return CatalogImportResponse.builder()
                .totalRows(stats.totalRows)
                .successRows(stats.successRows)
                .failedRows(stats.failedRows)
                .createdBrands(stats.createdBrands)
                .reusedBrands(stats.reusedBrands)
                .createdCategories(stats.createdCategories)
                .reusedCategories(stats.reusedCategories)
                .createdProducts(stats.createdProducts)
                .reusedProducts(stats.reusedProducts)
                .createdVariants(stats.createdVariants)
                .rows(rowResults)
                .build();
    }

    private CatalogImportResponse.CatalogImportRowResult importCatalogRow(
            Row row,
            DataFormatter formatter,
            int rowNumber,
            CatalogImportStats stats
    ) {
        String productName = readCell(row, 8, formatter);
        String productSlug = resolveSlug(readCell(row, 9, formatter), productName);
        String sku = readCell(row, 16, formatter);

        try {
            requireValue(productName, "Product name is required");
            requireValue(sku, "SKU is required");
            if (productVariantRepository.existsBySku(sku)) {
                throw new IllegalArgumentException("SKU already exists");
            }
            validateImportedRow(row, formatter);

            Product product = resolveImportedProduct(row, formatter, productName, productSlug, stats);
            createImportedVariant(row, formatter, product, sku);
            stats.createdVariants++;

            return CatalogImportResponse.CatalogImportRowResult.builder()
                    .rowNumber(rowNumber)
                    .productSlug(product.getSlug())
                    .sku(sku)
                    .success(true)
                    .message("Created")
                    .build();
        } catch (IllegalArgumentException | AppException e) {
            return CatalogImportResponse.CatalogImportRowResult.builder()
                    .rowNumber(rowNumber)
                    .productSlug(productSlug)
                    .sku(sku)
                    .success(false)
                    .message(rowErrorMessage(e))
                    .build();
        }
    }

    private Product resolveImportedProduct(
            Row row,
            DataFormatter formatter,
            String productName,
            String productSlug,
            CatalogImportStats stats
    ) {
        Optional<Product> existingProduct = productRepository.findBySlug(productSlug);
        if (existingProduct.isPresent()) {
            Product product = existingProduct.get();
            validateProductCatalogMatch(product, expectedBrandSlug(row, formatter), expectedCategorySlug(row, formatter));
            stats.reusedProducts++;
            return product;
        }

        Category category = resolveOrCreateCategory(row, formatter, stats);
        Brand brand = resolveOrCreateBrand(row, formatter, stats);
        return createImportedProduct(row, formatter, brand, category, productName, productSlug, stats);
    }

    private void validateImportedRow(Row row, DataFormatter formatter) {
        readPricingType(row, formatter);
        readBigDecimal(row, 18, formatter, "Price is required");
        readBigDecimal(row, 19, formatter, null);
        readBigDecimal(row, 20, formatter, null);
    }

    private Category resolveOrCreateCategory(Row row, DataFormatter formatter, CatalogImportStats stats) {
        String name = readCell(row, 0, formatter);
        if (name == null || name.isBlank()) {
            return null;
        }
        String slug = resolveSlug(readCell(row, 1, formatter), name);
        Optional<Category> existingCategory = categoryRepository.findBySlug(slug);
        if (existingCategory.isPresent()) {
            stats.reusedCategories++;
            return existingCategory.get();
        }

        Category category = Category.builder()
                .name(name)
                .slug(slug)
                .description(readCell(row, 2, formatter))
                .imageUrl(readCell(row, 3, formatter))
                .active(readBoolean(row, 22, formatter, true))
                .build();
        stats.createdCategories++;
        return categoryRepository.save(category);
    }

    private Brand resolveOrCreateBrand(Row row, DataFormatter formatter, CatalogImportStats stats) {
        String name = readCell(row, 4, formatter);
        if (name == null || name.isBlank()) {
            return null;
        }
        String slug = resolveSlug(readCell(row, 5, formatter), name);
        Optional<Brand> existingBrand = brandRepository.findBySlug(slug);
        if (existingBrand.isPresent()) {
            stats.reusedBrands++;
            return existingBrand.get();
        }

        Brand brand = Brand.builder()
                .name(name)
                .slug(slug)
                .description(readCell(row, 6, formatter))
                .logoUrl(readCell(row, 7, formatter))
                .active(readBoolean(row, 22, formatter, true))
                .build();
        stats.createdBrands++;
        return brandRepository.save(brand);
    }

    private Product createImportedProduct(
            Row row,
            DataFormatter formatter,
            Brand brand,
            Category category,
            String productName,
            String productSlug,
            CatalogImportStats stats
    ) {
        Product product = Product.builder()
                .name(productName)
                .slug(productSlug)
                .description(readCell(row, 10, formatter))
                .brand(brand)
                .category(category)
                .material(readCell(row, 11, formatter))
                .goldAge(readCell(row, 12, formatter))
                .stoneType(readCell(row, 13, formatter))
                .thumbnailUrl(readCell(row, 14, formatter))
                .pricingType(readPricingType(row, formatter))
                .active(readBoolean(row, 22, formatter, true))
                .build();
        stats.createdProducts++;
        return productRepository.save(product);
    }

    private void validateProductCatalogMatch(Product product, String expectedBrandSlug, String expectedCategorySlug) {
        String actualBrandSlug = product.getBrand() == null ? null : product.getBrand().getSlug();
        String actualCategorySlug = product.getCategory() == null ? null : product.getCategory().getSlug();

        if (!sameSlug(actualBrandSlug, expectedBrandSlug) || !sameSlug(actualCategorySlug, expectedCategorySlug)) {
            throw new IllegalArgumentException("Product slug already exists with different brand/category");
        }
    }

    private String expectedBrandSlug(Row row, DataFormatter formatter) {
        String brandName = readCell(row, 4, formatter);
        if (brandName == null || brandName.isBlank()) {
            return null;
        }
        return resolveSlug(readCell(row, 5, formatter), brandName);
    }

    private String expectedCategorySlug(Row row, DataFormatter formatter) {
        String categoryName = readCell(row, 0, formatter);
        if (categoryName == null || categoryName.isBlank()) {
            return null;
        }
        return resolveSlug(readCell(row, 1, formatter), categoryName);
    }

    private boolean sameSlug(String actualSlug, String expectedSlug) {
        if (actualSlug == null || actualSlug.isBlank()) {
            return expectedSlug == null || expectedSlug.isBlank();
        }
        return actualSlug.equals(expectedSlug);
    }

    private void createImportedVariant(Row row, DataFormatter formatter, Product product, String sku) {
        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .sku(sku)
                .size(readCell(row, 17, formatter))
                .price(readBigDecimal(row, 18, formatter, "Price is required"))
                .goldWeight(readBigDecimal(row, 19, formatter, null))
                .laborCost(readBigDecimal(row, 20, formatter, null))
                .imageUrl(readCell(row, 21, formatter))
                .active(readBoolean(row, 22, formatter, true))
                .build();
        productVariantRepository.save(variant);
    }

    private PricingType readPricingType(Row row, DataFormatter formatter) {
        String value = readCell(row, 15, formatter);
        if (value == null || value.isBlank()) {
            return PricingType.ABSOLUTE_FIXED;
        }
        try {
            return PricingType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Pricing type is invalid");
        }
    }

    private BigDecimal readBigDecimal(Row row, int cellIndex, DataFormatter formatter, String requiredMessage) {
        String value = readCell(row, cellIndex, formatter);
        if (value == null || value.isBlank()) {
            if (requiredMessage != null) {
                throw new IllegalArgumentException(requiredMessage);
            }
            return null;
        }
        try {
            return new BigDecimal(value.replace(",", "").trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Number value is invalid");
        }
    }

    private boolean readBoolean(Row row, int cellIndex, DataFormatter formatter, boolean defaultValue) {
        String value = readCell(row, cellIndex, formatter);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.equals("true")
                || normalized.equals("1")
                || normalized.equals("yes")
                || normalized.equals("y")
                || normalized.equals("active");
    }

    private void requireValue(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    private String readCell(Row row, int cellIndex, DataFormatter formatter) {
        if (row == null) {
            return null;
        }
        Cell cell = row.getCell(cellIndex);
        if (cell == null) {
            return null;
        }
        String value = formatter.formatCellValue(cell);
        return value == null || value.isBlank() ? null : value.trim();
    }

    private boolean isBlankRow(Row row, DataFormatter formatter) {
        if (row == null) {
            return true;
        }
        for (int cellIndex = 0; cellIndex <= 22; cellIndex++) {
            String value = readCell(row, cellIndex, formatter);
            if (value != null && !value.isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String rowErrorMessage(Exception exception) {
        if (exception instanceof AppException appException) {
            return appException.getCustomMessage() == null
                    ? appException.getErrorCode().getMessage()
                    : appException.getCustomMessage();
        }
        return exception.getMessage();
    }

    private static class CatalogImportStats {
        private int totalRows;
        private int successRows;
        private int failedRows;
        private int createdBrands;
        private int reusedBrands;
        private int createdCategories;
        private int reusedCategories;
        private int createdProducts;
        private int reusedProducts;
        private int createdVariants;
    }

    private Brand resolveBrand(String brandId) {
        if (brandId == null || brandId.isBlank()) {
            return null;
        }
        return brandRepository.findById(brandId)
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_EXIST));
    }

    private Category resolveCategory(String categoryId) {
        if (categoryId == null || categoryId.isBlank()) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXIST));
    }

    private String resolveSlug(String requestedSlug, String name) {
        return requestedSlug == null || requestedSlug.isBlank()
                ? SlugUtils.toSlug(name)
                : SlugUtils.toSlug(requestedSlug);
    }

    private void ensureSlugAvailable(boolean exists) {
        if (exists) {
            throw new AppException(ErrorCode.SLUG_EXISTED);
        }
    }

    private String brandFolder() {
        return CLOUDINARY_ROOT_FOLDER + "/brands";
    }

    private String categoryFolder() {
        return CLOUDINARY_ROOT_FOLDER + "/categories";
    }

    private String productFolder(String productId) {
        return CLOUDINARY_ROOT_FOLDER + "/products/" + productId;
    }

    private String variantFolder(ProductVariant variant) {
        return productFolder(variant.getProduct().getId()) + "/" + variant.getId();
    }
}
