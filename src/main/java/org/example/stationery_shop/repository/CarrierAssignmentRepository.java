package org.example.stationery_shop.repository;

import org.example.stationery_shop.entity.shipping.CarrierAssignment;
import org.example.stationery_shop.enums.CarrierAssignmentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

public interface CarrierAssignmentRepository extends JpaRepository<CarrierAssignment, String> {
    boolean existsByShipmentId(String shipmentId);

    @EntityGraph(attributePaths = {
            "carrier",
            "shipment",
            "shipment.order",
            "shipment.order.address",
            "shipment.order.store",
            "shipment.order.shippingFeeSnapshot"
    })
    List<CarrierAssignment> findByCarrierUserIdOrderByAssignedAtDesc(String userId);

    @EntityGraph(attributePaths = {
            "carrier",
            "shipment",
            "shipment.order",
            "shipment.order.address",
            "shipment.order.store",
            "shipment.order.shippingFeeSnapshot"
    })
    Optional<CarrierAssignment> findByIdAndCarrierUserId(String id, String userId);

    long countByCarrierIdAndStatusIn(String carrierId, Collection<CarrierAssignmentStatus> statuses);
}
