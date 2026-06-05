package org.example.stationery_shop.dto.response.checkout;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShippingServiceResponse {
    private Integer serviceId;
    private Integer serviceTypeId;
    private String name;
    private String shortName;
}
