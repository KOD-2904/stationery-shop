package org.example.stationery_shop.mapper;

import org.example.stationery_shop.dto.response.InventoryResponse;
import org.example.stationery_shop.dto.response.StoreAddressResponse;
import org.example.stationery_shop.dto.response.StoreResponse;
import org.example.stationery_shop.entity.inventory.Inventory;
import org.example.stationery_shop.entity.inventory.Store;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {
    public StoreResponse toStoreResponse(Store store) {
        if (store == null) {
            return null;
        }
        return StoreResponse.builder()
                .id(store.getId())
                .code(store.getCode())
                .name(store.getName())
                .address(toStoreAddressResponse(store))
                .phone(store.getPhone())
                .active(store.isActive())
                .build();
    }

    private StoreAddressResponse toStoreAddressResponse(Store store) {
        return StoreAddressResponse.builder()
                .provinceId(store.getProvinceId())
                .provinceName(store.getProvinceName())
                .districtId(store.getDistrictId())
                .districtName(store.getDistrictName())
                .wardCode(store.getWardCode())
                .wardName(store.getWardName())
                .detailAddress(store.getDetailAddress())
                .build();
    }

    public InventoryResponse toInventoryResponse(Inventory inventory) {
        if (inventory == null) {
            return null;
        }
        return InventoryResponse.builder()
                .id(inventory.getId())
                .productVariantId(inventory.getProductVariant().getId())
                .sku(inventory.getProductVariant().getSku())
                .storeId(inventory.getStore().getId())
                .storeCode(inventory.getStore().getCode())
                .quantityAvailable(inventory.getQuantityAvailable())
                .quantityLocked(inventory.getQuantityLocked())
                .quantityOnHand(inventory.getQuantityAvailable() + inventory.getQuantityLocked())
                .version(inventory.getVersion())
                .build();
    }
}
