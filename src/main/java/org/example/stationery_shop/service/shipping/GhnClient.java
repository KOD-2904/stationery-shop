package org.example.stationery_shop.service.shipping;

import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.entity.checkout.ShippingFeeSnapshot;
import org.example.stationery_shop.entity.customer.Address;
import org.example.stationery_shop.entity.order.Order;
import org.example.stationery_shop.exception.AppException;
import org.example.stationery_shop.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class GhnClient {
    private final GhnProperties properties;

    public GhnFeeResult calculateFee(Address address, Integer weight, Integer length, Integer width, Integer height,
                                     Integer serviceId, Integer serviceTypeId) {
        if (!properties.isEnabled()) {
            throw new AppException(ErrorCode.GHN_NOT_CONFIGURED);
        }
        // TODO: Wire GHN /shiip/public-api/v2/shipping-order/fee when shop credentials are available.
        return GhnFeeResult.builder()
                .fee(BigDecimal.ZERO)
                .serviceId(serviceId)
                .serviceTypeId(serviceTypeId)
                .build();
    }

    public GhnCreateOrderResult createOrder(Order order, ShippingFeeSnapshot snapshot) {
        if (!properties.isEnabled()) {
            throw new AppException(ErrorCode.GHN_NOT_CONFIGURED);
        }
        // TODO: Wire GHN /shiip/public-api/v2/shipping-order/create when shop credentials are available.
        return GhnCreateOrderResult.builder()
                .orderCode("TODO-GHN-" + order.getId())
                .totalFee(snapshot.getShippingFee())
                .build();
    }
}
