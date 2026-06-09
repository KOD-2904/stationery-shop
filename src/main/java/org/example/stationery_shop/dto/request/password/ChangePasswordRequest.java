package org.example.stationery_shop.dto.request.password;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.example.stationery_shop.validation.password.ValidPassword;

@Data
public class ChangePasswordRequest {
    @NotBlank(message = "Current password khong duoc rong")
    private String currentPassword;

    @ValidPassword(message = "Password phai >=8 ky tu, co chu hoa va so")
    private String newPassword;

    @NotBlank(message = "Confirm password khong duoc rong")
    private String confirmPassword;
}
