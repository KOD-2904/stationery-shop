package org.example.stationery_shop.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryChangeRequest {
    @NotBlank(message = "Product variant id is required")
    private String productVariantId;
    @NotBlank(message = "Store id is required")
    private String storeId;
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be greater than 0")
    private Integer quantity;
    private String note;
}
