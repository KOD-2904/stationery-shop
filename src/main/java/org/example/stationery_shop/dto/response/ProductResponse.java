package org.example.stationery_shop.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.stationery_shop.enums.PricingType;

import java.util.List;

@Getter
@Builder
public class ProductResponse {
    private String id;
    private String name;
    private String slug;
    private String description;
    private BrandResponse brand;
    private CategoryResponse category;
    private String material;
    private String goldAge;
    private String stoneType;
    private String thumbnailUrl;
    private PricingType pricingType;
    private boolean active;
    private List<ProductVariantResponse> variants;
    private List<ProductImageResponse> images;
}
