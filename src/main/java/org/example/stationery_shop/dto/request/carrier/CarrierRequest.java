package org.example.stationery_shop.dto.request.carrier;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CarrierRequest {
    private String userId;

    @NotBlank(message = "Carrier name is required")
    private String name;

    @NotBlank(message = "Carrier phone is required")
    private String phone;

    private Integer provinceId;
    private String provinceName;
    private Integer districtId;
    private String districtName;

    @NotNull(message = "Max active orders is required")
    @Min(value = 1, message = "Max active orders must be greater than 0")
    private Integer maxActiveOrders;

    private Boolean active = true;
}
