package org.example.stationery_shop.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.stationery_shop.enums.StoreDistanceLevel;

@Getter
@Builder
public class PickupStoreResponse {
    private StoreResponse store;
    private Integer quantityAvailable;
    private StoreDistanceLevel distanceLevel;
}
