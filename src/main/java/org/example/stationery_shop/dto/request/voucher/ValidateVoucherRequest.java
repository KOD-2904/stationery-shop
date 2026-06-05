package org.example.stationery_shop.dto.request.voucher;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ValidateVoucherRequest {
    @NotBlank(message = "Voucher code khong duoc rong")
    private String code;

    @NotNull(message = "Subtotal khong duoc rong")
    @DecimalMin(value = "0", message = "Subtotal khong duoc am")
    private BigDecimal subtotal;

    private List<String> productIds;
}
