package org.example.stationery_shop.repository;

import org.example.stationery_shop.entity.inventory.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, String> {
    List<InventoryTransaction> findByInventoryIdOrderByCreatedAtDesc(String inventoryId);
}
