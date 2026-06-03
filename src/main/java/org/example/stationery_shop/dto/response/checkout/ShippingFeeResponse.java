package org.example.stationery_shop.dto.response.checkout;

import lombok.Builder;
import lombok.Data;
import org.example.stationery_shop.enums.DeliveryMethod;
import org.example.stationery_shop.enums.ShippingProvider;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class ShippingFeeResponse {
    private String snapshotId;
    private DeliveryMethod deliveryMethod;
    private String addressId;
    private ShippingProvider provider;
    private BigDecimal shippingFee;
    private BigDecimal insuranceValue;
    private Integer serviceId;
    private Integer serviceTypeId;
    private Integer weight;
    private Integer length;
    private Integer width;
    private Integer height;
    private Instant expiredAt;
}
