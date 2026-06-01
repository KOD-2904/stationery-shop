package org.example.stationery_shop.repository;

import org.example.stationery_shop.entity.inventory.InventoryReservation;
import org.example.stationery_shop.enums.ReservationStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, String> {
    @EntityGraph(attributePaths = {"inventory", "inventory.productVariant", "inventory.store"})
    List<InventoryReservation> findByOrderId(String orderId);

    boolean existsByOrderIdAndStatus(String orderId, ReservationStatus status);
}
