package org.example.stationery_shop.entity.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.stationery_shop.entity.BaseEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "payment_webhook_log", uniqueConstraints = {
        @UniqueConstraint(name = "uk_payment_webhook_idempotency", columnNames = "idempotency_key")
})
public class PaymentWebhookLog extends BaseEntity {
    @Column(name = "provider", nullable = false, length = 30)
    private String provider;

    @Column(name = "idempotency_key", nullable = false, length = 150)
    private String idempotencyKey;

    @Lob
    @Column(name = "raw_payload", nullable = false)
    private String rawPayload;

    @Column(nullable = false)
    private boolean processed;
}
