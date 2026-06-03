package org.example.stationery_shop.entity.shipping;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.stationery_shop.entity.BaseEntity;
import org.example.stationery_shop.entity.order.Order;
import org.example.stationery_shop.enums.ShippingProvider;
import org.example.stationery_shop.enums.ShippingStatus;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "shipment", uniqueConstraints = {
        @UniqueConstraint(name = "uk_shipment_order", columnNames = "order_id"),
        @UniqueConstraint(name = "uk_shipment_ghn_order_code", columnNames = "ghn_order_code")
})
public class Shipment extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShippingProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ShippingStatus status;

    @Column(name = "ghn_order_code", length = 100)
    private String ghnOrderCode;

    @Column(name = "shipping_fee", nullable = false, precision = 18, scale = 2)
    private BigDecimal shippingFee;

    @Column(length = 1000)
    private String note;
}
