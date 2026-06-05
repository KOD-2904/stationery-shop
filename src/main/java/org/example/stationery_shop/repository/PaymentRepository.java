package org.example.stationery_shop.repository;

import org.example.stationery_shop.entity.payment.Payment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    @EntityGraph(attributePaths = {"order", "order.items", "order.address", "order.shippingFeeSnapshot"})
    Optional<Payment> findByOrderId(String orderId);

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.status = org.example.stationery_shop.enums.PaymentStatus.SUCCESS")
    BigDecimal sumSuccessfulAmount();

    @Query("""
            select coalesce(sum(p.amount), 0)
            from Payment p
            where p.status = org.example.stationery_shop.enums.PaymentStatus.SUCCESS
              and p.paidAt >= :startInclusive
              and p.paidAt < :endExclusive
            """)
    BigDecimal sumSuccessfulAmountBetween(Instant startInclusive, Instant endExclusive);
}
