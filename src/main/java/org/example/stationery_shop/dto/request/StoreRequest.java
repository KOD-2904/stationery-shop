package org.example.stationery_shop.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreRequest {
    @NotBlank(message = "Store code is required")
    private String code;
    @NotBlank(message = "Store name is required")
    private String name;

    @Valid
    @NotNull(message = "Store address is required")
    private StoreAddressRequest address;

    @NotBlank(message = "Store phone is required")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Store phone is invalid")
    private String phone;
    private Boolean active;
}
