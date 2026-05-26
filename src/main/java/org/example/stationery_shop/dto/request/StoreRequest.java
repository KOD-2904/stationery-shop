package org.example.stationery_shop.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreRequest {
    @NotBlank(message = "Store code is required")
    private String code;
    @NotBlank(message = "Store name is required")
    private String name;
    private String address;
    private String phone;
    private Boolean active;
}
