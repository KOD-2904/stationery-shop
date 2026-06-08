package org.example.stationery_shop.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PhoneOtpVerifyRequest {
    @NotBlank(message = "Phone khong duoc rong")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Phone khong hop le")
    private String phone;

    @NotBlank(message = "OTP khong duoc rong")
    @Pattern(regexp = "^[0-9]{6}$", message = "OTP phai gom 6 chu so")
    private String otp;
}
