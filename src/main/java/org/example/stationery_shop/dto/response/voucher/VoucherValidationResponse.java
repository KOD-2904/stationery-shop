package org.example.stationery_shop.dto.response.voucher;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class VoucherValidationResponse {
    private String code;
    private boolean valid;
    private String message;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal totalAfterDiscount;
}
