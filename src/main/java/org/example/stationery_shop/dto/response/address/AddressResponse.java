package org.example.stationery_shop.dto.response.address;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class AddressResponse {
    private String id;
    private String receiverName;
    private String receiverPhone;
    private Integer provinceId;
    private String provinceName;
    private Integer districtId;
    private String districtName;
    private String wardCode;
    private String wardName;
    private String detailAddress;
    private boolean defaultAddress;
    private Instant createdAt;
    private Instant updatedAt;
}
