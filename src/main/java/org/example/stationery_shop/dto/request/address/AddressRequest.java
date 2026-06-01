package org.example.stationery_shop.dto.request.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddressRequest {
    @NotBlank(message = "Ten nguoi nhan khong duoc rong")
    private String receiverName;

    @NotBlank(message = "So dien thoai nguoi nhan khong duoc rong")
    private String receiverPhone;

    @NotNull(message = "Province id khong duoc rong")
    private Integer provinceId;

    @NotBlank(message = "Province name khong duoc rong")
    private String provinceName;

    @NotNull(message = "District id khong duoc rong")
    private Integer districtId;

    @NotBlank(message = "District name khong duoc rong")
    private String districtName;

    @NotBlank(message = "Ward code khong duoc rong")
    private String wardCode;

    @NotBlank(message = "Ward name khong duoc rong")
    private String wardName;

    @NotBlank(message = "Dia chi chi tiet khong duoc rong")
    private String detailAddress;

    private Boolean defaultAddress;
}
