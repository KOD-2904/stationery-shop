package org.example.stationery_shop.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.request.voucher.ValidateVoucherRequest;
import org.example.stationery_shop.dto.request.voucher.VoucherRequest;
import org.example.stationery_shop.dto.response.CategoryResponse;
import org.example.stationery_shop.dto.response.voucher.VoucherResponse;
import org.example.stationery_shop.dto.response.voucher.VoucherValidationResponse;
import org.example.stationery_shop.entity.catalog.Category;
import org.example.stationery_shop.entity.catalog.Product;
import org.example.stationery_shop.entity.order.Order;
import org.example.stationery_shop.entity.voucher.Voucher;
import org.example.stationery_shop.entity.voucher.VoucherUsage;
import org.example.stationery_shop.enums.VoucherDiscountType;
import org.example.stationery_shop.exception.AppException;
import org.example.stationery_shop.exception.ErrorCode;
import org.example.stationery_shop.mapper.CatalogMapper;
import org.example.stationery_shop.repository.CategoryRepository;
import org.example.stationery_shop.repository.OrderRepository;
import org.example.stationery_shop.repository.ProductRepository;
import org.example.stationery_shop.repository.VoucherRepository;
import org.example.stationery_shop.repository.VoucherUsageRepository;
import org.example.stationery_shop.service.VoucherService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final VoucherRepository voucherRepository;
    private final VoucherUsageRepository voucherUsageRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final CatalogMapper catalogMapper;

    @Override
    @Transactional(readOnly = true)
    public List<VoucherResponse> getVouchers(boolean includeInactive) {
        List<Voucher> vouchers = includeInactive
                ? voucherRepository.findAllByOrderByCreatedAtDesc()
                : voucherRepository.findByActiveTrueOrderByCreatedAtDesc();
        return vouchers.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public VoucherResponse createVoucher(VoucherRequest request) {
        String code = normalizeCode(request.getCode());
        if (voucherRepository.existsByCodeIgnoreCase(code)) {
            throw new AppException(ErrorCode.VOUCHER_CODE_EXISTED);
        }
        validateVoucherRequest(request);
        Voucher voucher = Voucher.builder()
                .code(code)
                .name(request.getName())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .minOrderAmount(request.getMinOrderAmount())
                .usageLimit(request.getUsageLimit())
                .usedCount(0)
                .startsAt(request.getStartsAt())
                .endsAt(request.getEndsAt())
                .active(request.getActive() == null || request.getActive())
                .categories(resolveCategories(request.getCategoryIds()))
                .build();
        return toResponse(voucherRepository.save(voucher));
    }

    @Override
    @Transactional
    public VoucherResponse updateVoucher(String id, VoucherRequest request) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_EXIST));
        String code = normalizeCode(request.getCode());
        if (!voucher.getCode().equalsIgnoreCase(code) && voucherRepository.existsByCodeIgnoreCase(code)) {
            throw new AppException(ErrorCode.VOUCHER_CODE_EXISTED);
        }
        validateVoucherRequest(request);
        voucher.setCode(code);
        voucher.setName(request.getName());
        voucher.setDiscountType(request.getDiscountType());
        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setMaxDiscountAmount(request.getMaxDiscountAmount());
        voucher.setMinOrderAmount(request.getMinOrderAmount());
        voucher.setUsageLimit(request.getUsageLimit());
        voucher.setStartsAt(request.getStartsAt());
        voucher.setEndsAt(request.getEndsAt());
        voucher.setActive(request.getActive() == null || request.getActive());
        voucher.setCategories(resolveCategories(request.getCategoryIds()));
        return toResponse(voucherRepository.save(voucher));
    }

    @Override
    @Transactional(readOnly = true)
    public VoucherValidationResponse validateVoucher(ValidateVoucherRequest request) {
        return validateVoucher(request.getCode(), request.getSubtotal(), request.getProductIds());
    }

    @Override
    @Transactional(readOnly = true)
    public VoucherValidationResponse validateVoucher(String code, BigDecimal subtotal, List<String> productIds) {
        Voucher voucher = voucherRepository.findByCodeIgnoreCase(normalizeCode(code))
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_EXIST));
        validateAvailability(voucher, subtotal, productIds);
        BigDecimal discountAmount = calculateDiscount(voucher, subtotal);
        return VoucherValidationResponse.builder()
                .code(voucher.getCode())
                .valid(true)
                .message("Voucher hop le")
                .subtotal(subtotal)
                .discountAmount(discountAmount)
                .totalAfterDiscount(subtotal.subtract(discountAmount))
                .build();
    }

    @Override
    @Transactional
    public void recordUsage(String code, String orderId) {
        if (!StringUtils.hasText(code) || voucherUsageRepository.existsByOrderId(orderId)) {
            return;
        }
        Voucher voucher = voucherRepository.findByCodeIgnoreCase(normalizeCode(code))
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_EXIST));
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXIST));
        voucher.setUsedCount(voucher.getUsedCount() + 1);
        voucherRepository.save(voucher);
        voucherUsageRepository.save(VoucherUsage.builder()
                .voucher(voucher)
                .user(order.getUser())
                .order(order)
                .build());
    }

    private void validateAvailability(Voucher voucher, BigDecimal subtotal, List<String> productIds) {
        Instant now = Instant.now();
        if (!voucher.isActive()
                || voucher.getStartsAt().isAfter(now)
                || voucher.getEndsAt().isBefore(now)
                || subtotal.compareTo(voucher.getMinOrderAmount()) < 0
                || isUsageLimitReached(voucher)
                || !matchesCategory(voucher, productIds)) {
            throw new AppException(ErrorCode.VOUCHER_NOT_AVAILABLE);
        }
    }

    private BigDecimal calculateDiscount(Voucher voucher, BigDecimal subtotal) {
        BigDecimal discount = voucher.getDiscountType() == VoucherDiscountType.PERCENT
                ? subtotal.multiply(voucher.getDiscountValue()).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP)
                : voucher.getDiscountValue();
        if (voucher.getMaxDiscountAmount() != null && voucher.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            discount = discount.min(voucher.getMaxDiscountAmount());
        }
        return discount.min(subtotal);
    }

    private boolean matchesCategory(Voucher voucher, List<String> productIds) {
        if (voucher.getCategories() == null || voucher.getCategories().isEmpty()) {
            return true;
        }
        if (productIds == null || productIds.isEmpty()) {
            return false;
        }
        Set<String> categoryIds = voucher.getCategories().stream()
                .map(Category::getId)
                .collect(java.util.stream.Collectors.toSet());
        return productRepository.findAllById(productIds)
                .stream()
                .map(Product::getCategory)
                .filter(category -> category != null)
                .map(Category::getId)
                .anyMatch(categoryIds::contains);
    }

    private boolean isUsageLimitReached(Voucher voucher) {
        return voucher.getUsageLimit() != null
                && voucher.getUsageLimit() > 0
                && voucher.getUsedCount() >= voucher.getUsageLimit();
    }

    private void validateVoucherRequest(VoucherRequest request) {
        if (request.getEndsAt().isBefore(request.getStartsAt())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Thoi gian ket thuc phai sau thoi gian bat dau");
        }
        if (request.getDiscountType() == VoucherDiscountType.PERCENT
                && request.getDiscountValue().compareTo(ONE_HUNDRED) > 0) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Voucher percent khong duoc vuot qua 100");
        }
        if (request.getUsageLimit() != null && request.getUsageLimit() < 0) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Usage limit khong duoc am");
        }
    }

    private Set<Category> resolveCategories(List<String> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Category> categories = categoryRepository.findAllById(categoryIds);
        if (categories.size() != categoryIds.size()) {
            throw new AppException(ErrorCode.CATEGORY_NOT_EXIST);
        }
        return new HashSet<>(categories);
    }

    private VoucherResponse toResponse(Voucher voucher) {
        List<CategoryResponse> categories = voucher.getCategories() == null
                ? List.of()
                : voucher.getCategories().stream()
                .map(catalogMapper::toCategoryResponse)
                .toList();
        return VoucherResponse.builder()
                .id(voucher.getId())
                .code(voucher.getCode())
                .name(voucher.getName())
                .discountType(voucher.getDiscountType())
                .discountValue(voucher.getDiscountValue())
                .maxDiscountAmount(voucher.getMaxDiscountAmount())
                .minOrderAmount(voucher.getMinOrderAmount())
                .usageLimit(voucher.getUsageLimit())
                .usedCount(voucher.getUsedCount())
                .startsAt(voucher.getStartsAt())
                .endsAt(voucher.getEndsAt())
                .active(voucher.isActive())
                .categories(categories)
                .build();
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(java.util.Locale.ROOT);
    }
}
