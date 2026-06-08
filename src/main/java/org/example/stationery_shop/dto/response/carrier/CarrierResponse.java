package org.example.stationery_shop.dto.response.carrier;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CarrierResponse {
    private String id;
    private String userId;
    private String userEmail;
    private String userName;
    private String name;
    private String phone;
    private Integer provinceId;
    private String provinceName;
    private Integer districtId;
    private String districtName;
    private Integer currentAssignedOrders;
    private Integer maxActiveOrders;
    private boolean active;
}
