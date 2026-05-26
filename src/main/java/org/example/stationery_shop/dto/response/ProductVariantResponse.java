package org.example.stationery_shop.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class ProductVariantResponse {
    private String id;
    private String productId;
    private String sku;
    private String size;
    private BigDecimal price;
    private BigDecimal goldWeight;
    private BigDecimal laborCost;
    private String imageUrl;
    private boolean active;
    private Long version;
    private List<ProductVariantImageResponse> images;
}
