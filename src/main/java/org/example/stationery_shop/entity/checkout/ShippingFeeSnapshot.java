package org.example.stationery_shop.entity.checkout;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.stationery_shop.entity.BaseEntity;
import org.example.stationery_shop.entity.auth.User;
import org.example.stationery_shop.entity.customer.Address;
import org.example.stationery_shop.enums.DeliveryMethod;
import org.example.stationery_shop.enums.ShippingProvider;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "shipping_fee_snapshot")
public class ShippingFeeSnapshot extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_method", nullable = false, length = 30)
    private DeliveryMethod deliveryMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShippingProvider provider;

    @Column(name = "shipping_fee", nullable = false, precision = 18, scale = 2)
    private BigDecimal shippingFee;

    @Builder.Default
    @Column(name = "insurance_value", precision = 18, scale = 2)
    private BigDecimal insuranceValue = BigDecimal.ZERO;

    @Column(name = "service_id")
    private Integer serviceId;

    @Column(name = "service_type_id")
    private Integer serviceTypeId;

    @Column(nullable = false)
    private Integer weight;

    @Column(nullable = false)
    private Integer length;

    @Column(nullable = false)
    private Integer width;

    @Column(nullable = false)
    private Integer height;

    @Column(name = "expired_at", nullable = false)
    private Instant expiredAt;

    @Column(nullable = false)
    private boolean used;
}
