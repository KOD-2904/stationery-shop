package org.example.stationery_shop.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoreAddressResponse {
    private Integer provinceId;
    private String provinceName;
    private Integer districtId;
    private String districtName;
    private String wardCode;
    private String wardName;
    private String detailAddress;
}
