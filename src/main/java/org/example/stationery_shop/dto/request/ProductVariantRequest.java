package org.example.stationery_shop.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductVariantRequest {
    @NotBlank(message = "SKU is required")
    private String sku;
    private String size;
    @NotNull(message = "Price is required")
    @PositiveOrZero(message = "Price must be greater than or equal to 0")
    private BigDecimal price;
    @PositiveOrZero(message = "Gold weight must be greater than or equal to 0")
    private BigDecimal goldWeight;
    @PositiveOrZero(message = "Labor cost must be greater than or equal to 0")
    private BigDecimal laborCost;
    private Boolean active;
}
