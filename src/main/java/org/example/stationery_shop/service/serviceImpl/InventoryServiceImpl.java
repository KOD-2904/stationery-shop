package org.example.stationery_shop.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.request.InventoryAdjustRequest;
import org.example.stationery_shop.dto.request.InventoryChangeRequest;
import org.example.stationery_shop.dto.request.StoreAddressRequest;
import org.example.stationery_shop.dto.request.StoreRequest;
import org.example.stationery_shop.dto.response.InventoryResponse;
import org.example.stationery_shop.dto.response.PickupStoreResponse;
import org.example.stationery_shop.dto.response.StoreResponse;
import org.example.stationery_shop.entity.catalog.ProductVariant;
import org.example.stationery_shop.entity.inventory.Inventory;
import org.example.stationery_shop.entity.inventory.InventoryTransaction;
import org.example.stationery_shop.entity.inventory.Store;
import org.example.stationery_shop.enums.InventoryTransactionType;
import org.example.stationery_shop.enums.StoreDistanceLevel;
import org.example.stationery_shop.exception.AppException;
import org.example.stationery_shop.exception.ErrorCode;
import org.example.stationery_shop.mapper.InventoryMapper;
import org.example.stationery_shop.repository.InventoryRepository;
import org.example.stationery_shop.repository.InventoryTransactionRepository;
import org.example.stationery_shop.repository.ProductVariantRepository;
import org.example.stationery_shop.repository.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements org.example.stationery_shop.service.InventoryService {
    private final StoreRepository storeRepository;
    private final ProductVariantRepository productVariantRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final InventoryMapper inventoryMapper;

    @Override
    public List<StoreResponse> getStores(boolean includeInactive) {
        List<Store> stores = includeInactive
                ? storeRepository.findAll()
                : storeRepository.findByActiveTrueOrderByNameAsc();
        return stores.stream().map(inventoryMapper::toStoreResponse).toList();
    }

    @Override
    @Transactional
    public StoreResponse createStore(StoreRequest request) {
        if (storeRepository.existsByCode(request.getCode())) {
            throw new AppException(ErrorCode.STORE_CODE_EXISTED);
        }
        Store store = Store.builder()
                .code(request.getCode())
                .name(request.getName())
                .phone(request.getPhone())
                .active(request.getActive() == null || request.getActive())
                .build();
        applyStoreAddress(store, request.getAddress());
        return inventoryMapper.toStoreResponse(storeRepository.save(store));
    }

    @Override
    @Transactional
    public StoreResponse updateStore(String id, StoreRequest request) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_EXIST));
        if (!store.getCode().equals(request.getCode()) && storeRepository.existsByCode(request.getCode())) {
            throw new AppException(ErrorCode.STORE_CODE_EXISTED);
        }
        store.setCode(request.getCode());
        store.setName(request.getName());
        store.setPhone(request.getPhone());
        applyStoreAddress(store, request.getAddress());
        if (request.getActive() != null) {
            store.setActive(request.getActive());
        }
        return inventoryMapper.toStoreResponse(storeRepository.save(store));
    }

    @Override
    public List<PickupStoreResponse> findPickupStores(String productVariantId, Integer quantity,
                                                      Integer provinceId, Integer districtId, String wardCode) {
        int requestedQuantity = quantity == null || quantity < 1 ? 1 : quantity;
        productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXIST));
        return inventoryRepository.findPickupCandidates(productVariantId, requestedQuantity)
                .stream()
                .map(inventory -> toPickupStoreResponse(inventory, provinceId, districtId, wardCode))
                .sorted(Comparator
                        .comparing((PickupStoreResponse response) -> response.getDistanceLevel().ordinal())
                        .thenComparing(response -> response.getStore().getName()))
                .toList();
    }

    @Override
    public List<InventoryResponse> getInventoryByVariant(String productVariantId) {
        return inventoryRepository.findByProductVariantId(productVariantId)
                .stream()
                .map(inventoryMapper::toInventoryResponse)
                .toList();
    }

    @Override
    @Transactional
    public InventoryResponse importStock(InventoryChangeRequest request) {
        Inventory inventory = getOrCreateInventory(request.getProductVariantId(), request.getStoreId());
        int availableBefore = inventory.getQuantityAvailable();
        int lockedBefore = inventory.getQuantityLocked();
        inventory.setQuantityAvailable(availableBefore + request.getQuantity());
        Inventory saved = inventoryRepository.save(inventory);
        saveTransaction(saved, InventoryTransactionType.IMPORT, request.getQuantity(), availableBefore, lockedBefore, request.getNote());
        return inventoryMapper.toInventoryResponse(saved);
    }

    @Override
    @Transactional
    public InventoryResponse adjustStock(InventoryAdjustRequest request) {
        Inventory inventory = getOrCreateInventory(request.getProductVariantId(), request.getStoreId());
        int availableBefore = inventory.getQuantityAvailable();
        int lockedBefore = inventory.getQuantityLocked();
        inventory.setQuantityAvailable(request.getQuantityAvailable());
        Inventory saved = inventoryRepository.save(inventory);
        saveTransaction(saved, InventoryTransactionType.ADJUST, request.getQuantityAvailable() - availableBefore, availableBefore, lockedBefore, request.getNote());
        return inventoryMapper.toInventoryResponse(saved);
    }

    @Override
    @Transactional
    public InventoryResponse lockStock(InventoryChangeRequest request) {
        Inventory inventory = getLockedInventory(request.getProductVariantId(), request.getStoreId());
        int availableBefore = inventory.getQuantityAvailable();
        int lockedBefore = inventory.getQuantityLocked();
        if (availableBefore < request.getQuantity()) {
            throw new AppException(ErrorCode.NOT_ENOUGH_STOCK);
        }
        inventory.setQuantityAvailable(availableBefore - request.getQuantity());
        inventory.setQuantityLocked(lockedBefore + request.getQuantity());
        Inventory saved = inventoryRepository.save(inventory);
        saveTransaction(saved, InventoryTransactionType.LOCK, request.getQuantity(), availableBefore, lockedBefore, request.getNote());
        return inventoryMapper.toInventoryResponse(saved);
    }

    @Override
    @Transactional
    public InventoryResponse releaseStock(InventoryChangeRequest request) {
        Inventory inventory = getLockedInventory(request.getProductVariantId(), request.getStoreId());
        int availableBefore = inventory.getQuantityAvailable();
        int lockedBefore = inventory.getQuantityLocked();
        if (lockedBefore < request.getQuantity()) {
            throw new AppException(ErrorCode.NOT_ENOUGH_LOCKED_STOCK);
        }
        inventory.setQuantityAvailable(availableBefore + request.getQuantity());
        inventory.setQuantityLocked(lockedBefore - request.getQuantity());
        Inventory saved = inventoryRepository.save(inventory);
        saveTransaction(saved, InventoryTransactionType.RELEASE, request.getQuantity(), availableBefore, lockedBefore, request.getNote());
        return inventoryMapper.toInventoryResponse(saved);
    }

    @Override
    @Transactional
    public InventoryResponse deductStock(InventoryChangeRequest request) {
        Inventory inventory = getLockedInventory(request.getProductVariantId(), request.getStoreId());
        int availableBefore = inventory.getQuantityAvailable();
        int lockedBefore = inventory.getQuantityLocked();
        if (lockedBefore < request.getQuantity()) {
            throw new AppException(ErrorCode.NOT_ENOUGH_LOCKED_STOCK);
        }
        inventory.setQuantityLocked(lockedBefore - request.getQuantity());
        Inventory saved = inventoryRepository.save(inventory);
        saveTransaction(saved, InventoryTransactionType.DEDUCT, request.getQuantity(), availableBefore, lockedBefore, request.getNote());
        return inventoryMapper.toInventoryResponse(saved);
    }

    private Inventory getOrCreateInventory(String productVariantId, String storeId) {
        return inventoryRepository.findLockedByProductVariantIdAndStoreId(productVariantId, storeId)
                .orElseGet(() -> {
                    ProductVariant variant = productVariantRepository.findById(productVariantId)
                            .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXIST));
                    Store store = storeRepository.findById(storeId)
                            .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_EXIST));
                    return inventoryRepository.save(Inventory.builder()
                            .productVariant(variant)
                            .store(store)
                            .quantityAvailable(0)
                            .quantityLocked(0)
                            .build());
                });
    }

    private Inventory getLockedInventory(String productVariantId, String storeId) {
        return inventoryRepository.findLockedByProductVariantIdAndStoreId(productVariantId, storeId)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_EXIST));
    }

    private void saveTransaction(
            Inventory inventory,
            InventoryTransactionType type,
            int quantity,
            int availableBefore,
            int lockedBefore,
            String note
    ) {
        inventoryTransactionRepository.save(InventoryTransaction.builder()
                .inventory(inventory)
                .type(type)
                .quantity(quantity)
                .availableBefore(availableBefore)
                .availableAfter(inventory.getQuantityAvailable())
                .lockedBefore(lockedBefore)
                .lockedAfter(inventory.getQuantityLocked())
                .note(note)
                .build());
    }

    private void applyStoreAddress(Store store, StoreAddressRequest address) {
        store.setProvinceId(address.getProvinceId());
        store.setProvinceName(address.getProvinceName());
        store.setDistrictId(address.getDistrictId());
        store.setDistrictName(address.getDistrictName());
        store.setWardCode(address.getWardCode());
        store.setWardName(address.getWardName());
        store.setDetailAddress(address.getDetailAddress());
    }

    private PickupStoreResponse toPickupStoreResponse(Inventory inventory, Integer provinceId,
                                                      Integer districtId, String wardCode) {
        return PickupStoreResponse.builder()
                .store(inventoryMapper.toStoreResponse(inventory.getStore()))
                .quantityAvailable(inventory.getQuantityAvailable())
                .distanceLevel(resolveDistanceLevel(inventory.getStore(), provinceId, districtId, wardCode))
                .build();
    }

    private StoreDistanceLevel resolveDistanceLevel(Store store, Integer provinceId, Integer districtId, String wardCode) {
        if (wardCode != null && !wardCode.isBlank() && wardCode.equals(store.getWardCode())) {
            return StoreDistanceLevel.SAME_WARD;
        }
        if (districtId != null && districtId.equals(store.getDistrictId())) {
            return StoreDistanceLevel.SAME_DISTRICT;
        }
        if (provinceId != null && provinceId.equals(store.getProvinceId())) {
            return StoreDistanceLevel.SAME_PROVINCE;
        }
        return StoreDistanceLevel.OTHER;
    }
}
