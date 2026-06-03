package org.example.stationery_shop.dto.response.cart;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UnavailableCartItemResponse {
    private String cartItemId;
    private String productVariantId;
    private String sku;
    private String productName;
    private Integer requestedQuantity;
    private Integer availableQuantity;
}
