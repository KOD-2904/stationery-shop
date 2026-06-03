package org.example.stationery_shop.dto.response.cart;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CartItemResponse {
    private String id;
    private String productVariantId;
    private String sku;
    private String productName;
    private String variantSize;
    private String storeId;
    private String storeCode;
    private Integer quantity;
    private Integer availableQuantity;
    private boolean availableForCheckout;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
}
