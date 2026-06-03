package org.example.stationery_shop.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreAddressRequest {
    @NotNull(message = "Province id is required")
    private Integer provinceId;

    @NotBlank(message = "Province name is required")
    private String provinceName;

    @NotNull(message = "District id is required")
    private Integer districtId;

    @NotBlank(message = "District name is required")
    private String districtName;

    @NotBlank(message = "Ward code is required")
    private String wardCode;

    @NotBlank(message = "Ward name is required")
    private String wardName;

    @NotBlank(message = "Detail address is required")
    private String detailAddress;
}
