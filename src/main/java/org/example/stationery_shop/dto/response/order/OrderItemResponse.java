package org.example.stationery_shop.dto.response.order;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemResponse {
    private String productVariantId;
    private String sku;
    private String productName;
    private String variantSize;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
}
