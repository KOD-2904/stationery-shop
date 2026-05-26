package org.example.stationery_shop.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductVariantImageResponse {
    private String id;
    private String imageUrl;
    private boolean primaryImage;
    private Integer sortOrder;
}
