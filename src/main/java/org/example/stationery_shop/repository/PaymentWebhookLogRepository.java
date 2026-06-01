package org.example.stationery_shop.repository;

import org.example.stationery_shop.entity.payment.PaymentWebhookLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentWebhookLogRepository extends JpaRepository<PaymentWebhookLog, String> {
    Optional<PaymentWebhookLog> findByIdempotencyKey(String idempotencyKey);
}
