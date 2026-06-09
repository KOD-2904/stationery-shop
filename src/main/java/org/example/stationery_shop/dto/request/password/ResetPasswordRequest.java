package org.example.stationery_shop.dto.request.password;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.example.stationery_shop.validation.email.ValidEmail;
import org.example.stationery_shop.validation.password.ValidPassword;

@Data
public class ResetPasswordRequest {
    @ValidEmail(message = "Email phai dung dinh dang")
    private String email;

    @NotBlank(message = "OTP khong duoc rong")
    @Pattern(regexp = "^[0-9]{6}$", message = "OTP phai gom 6 chu so")
    private String otp;

    @ValidPassword(message = "Password phai >=8 ky tu, co chu hoa va so")
    private String newPassword;

    @NotBlank(message = "Confirm password khong duoc rong")
    private String confirmPassword;
}
