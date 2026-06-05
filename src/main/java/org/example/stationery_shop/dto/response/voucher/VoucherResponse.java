package org.example.stationery_shop.dto.response.voucher;

import lombok.Builder;
import lombok.Data;
import org.example.stationery_shop.dto.response.CategoryResponse;
import org.example.stationery_shop.enums.VoucherDiscountType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class VoucherResponse {
    private String id;
    private String code;
    private String name;
    private VoucherDiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal maxDiscountAmount;
    private BigDecimal minOrderAmount;
    private Integer usageLimit;
    private Integer usedCount;
    private Instant startsAt;
    private Instant endsAt;
    private boolean active;
    private List<CategoryResponse> categories;
}
