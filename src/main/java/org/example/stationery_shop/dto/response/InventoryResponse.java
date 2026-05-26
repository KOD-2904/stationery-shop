package org.example.stationery_shop.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InventoryResponse {
    private String id;
    private String productVariantId;
    private String sku;
    private String storeId;
    private String storeCode;
    private Integer quantityAvailable;
    private Integer quantityLocked;
    private Integer quantityOnHand;
    private Long version;
}
