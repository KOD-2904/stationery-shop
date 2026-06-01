package org.example.stationery_shop.repository;

import org.example.stationery_shop.entity.shipping.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, String> {
    Optional<Shipment> findByOrderId(String orderId);
}
