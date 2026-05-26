package org.example.stationery_shop.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.example.stationery_shop.enums.PricingType;

@Getter
@Setter
public class ProductRequest {
    @NotBlank(message = "Product name is required")
    private String name;
    private String slug;
    private String description;
    private String brandId;
    private String categoryId;
    private String material;
    private String goldAge;
    private String stoneType;
    private String thumbnailUrl;
    private PricingType pricingType;
    private Boolean active;
}
