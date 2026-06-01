package org.example.stationery_shop.repository;

import org.example.stationery_shop.entity.checkout.ShippingFeeSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShippingFeeSnapshotRepository extends JpaRepository<ShippingFeeSnapshot, String> {
    Optional<ShippingFeeSnapshot> findByIdAndUserId(String id, String userId);
}
