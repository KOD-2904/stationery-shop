package org.example.stationery_shop.repository;

import jakarta.persistence.LockModeType;
import org.example.stationery_shop.entity.inventory.Inventory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, String> {
    @EntityGraph(attributePaths = {"productVariant", "store"})
    List<Inventory> findByProductVariantId(String productVariantId);

    @EntityGraph(attributePaths = {"productVariant", "store"})
    Optional<Inventory> findByProductVariantIdAndStoreId(String productVariantId, String storeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"productVariant", "store"})
    Optional<Inventory> findLockedByProductVariantIdAndStoreId(String productVariantId, String storeId);
}
