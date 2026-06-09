package org.example.stationery_shop.dto.request.password;

import lombok.Data;
import org.example.stationery_shop.validation.email.ValidEmail;

@Data
public class ForgotPasswordOtpRequest {
    @ValidEmail(message = "Email phai dung dinh dang")
    private String email;
}
