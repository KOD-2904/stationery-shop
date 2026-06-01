package org.example.stationery_shop.dto.request.checkout;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.stationery_shop.enums.DeliveryMethod;

@Data
public class ShippingFeeRequest {
    @NotNull(message = "Delivery method khong duoc rong")
    private DeliveryMethod deliveryMethod;
    private String addressId;
    private Integer weight;
    private Integer length;
    private Integer width;
    private Integer height;
    private Integer serviceId;
    private Integer serviceTypeId;
}
