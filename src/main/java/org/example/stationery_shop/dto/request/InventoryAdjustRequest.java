package org.example.stationery_shop.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryAdjustRequest {
    @NotBlank(message = "Product variant id is required")
    private String productVariantId;
    @NotBlank(message = "Store id is required")
    private String storeId;
    @NotNull(message = "Quantity available is required")
    @Min(value = 0, message = "Quantity available must be greater than or equal to 0")
    private Integer quantityAvailable;
    private String note;
}
