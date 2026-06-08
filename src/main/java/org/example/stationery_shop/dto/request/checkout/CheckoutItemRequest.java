package org.example.stationery_shop.dto.request.checkout;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckoutItemRequest {
    @NotBlank(message = "Product variant id khong duoc rong")
    private String productVariantId;

    private String storeId;

    @NotNull(message = "Quantity khong duoc rong")
    @Min(value = 1, message = "Quantity phai lon hon 0")
    private Integer quantity;
}
