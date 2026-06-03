package org.example.stationery_shop.repository;

import org.example.stationery_shop.entity.payment.Payment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    @EntityGraph(attributePaths = {"order", "order.items", "order.address", "order.shippingFeeSnapshot"})
    Optional<Payment> findByOrderId(String orderId);

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
}
