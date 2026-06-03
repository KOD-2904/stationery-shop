package org.example.stationery_shop.service;

import org.example.stationery_shop.dto.request.InventoryAdjustRequest;
import org.example.stationery_shop.dto.request.InventoryChangeRequest;
import org.example.stationery_shop.dto.request.StoreRequest;
import org.example.stationery_shop.dto.response.InventoryResponse;
import org.example.stationery_shop.dto.response.PickupStoreResponse;
import org.example.stationery_shop.dto.response.StoreResponse;

import java.util.List;

public interface InventoryService {
    List<StoreResponse> getStores(boolean includeInactive);
    StoreResponse createStore(StoreRequest request);
    StoreResponse updateStore(String id, StoreRequest request);
    List<PickupStoreResponse> findPickupStores(String productVariantId, Integer quantity,
                                               Integer provinceId, Integer districtId, String wardCode);

    List<InventoryResponse> getInventoryByVariant(String productVariantId);
    InventoryResponse importStock(InventoryChangeRequest request);
    InventoryResponse adjustStock(InventoryAdjustRequest request);
    InventoryResponse lockStock(InventoryChangeRequest request);
    InventoryResponse releaseStock(InventoryChangeRequest request);
    InventoryResponse deductStock(InventoryChangeRequest request);
}
