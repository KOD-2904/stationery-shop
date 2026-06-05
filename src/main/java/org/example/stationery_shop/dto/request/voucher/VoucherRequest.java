package org.example.stationery_shop.dto.request.voucher;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.example.stationery_shop.enums.VoucherDiscountType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class VoucherRequest {
    @NotBlank(message = "Voucher code khong duoc rong")
    @Size(max = 100, message = "Voucher code khong duoc vuot qua 100 ky tu")
    private String code;

    @NotBlank(message = "Voucher name khong duoc rong")
    private String name;

    @NotNull(message = "Discount type khong duoc rong")
    private VoucherDiscountType discountType;

    @NotNull(message = "Discount value khong duoc rong")
    @DecimalMin(value = "0.01", message = "Discount value phai lon hon 0")
    private BigDecimal discountValue;

    private BigDecimal maxDiscountAmount;

    @NotNull(message = "Min order amount khong duoc rong")
    @DecimalMin(value = "0", message = "Min order amount khong duoc am")
    private BigDecimal minOrderAmount;

    private Integer usageLimit;

    @NotNull(message = "Starts at khong duoc rong")
    private Instant startsAt;

    @NotNull(message = "Ends at khong duoc rong")
    private Instant endsAt;

    private Boolean active;
    private List<String> categoryIds;
}
