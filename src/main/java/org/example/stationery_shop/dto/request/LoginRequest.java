package org.example.stationery_shop.dto.request;

import lombok.*;
import org.example.stationery_shop.validation.email.ValidEmail;
import org.example.stationery_shop.validation.password.ValidPassword;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    @ValidEmail(message = "Email phải đúng định dạng")
    private String email;
    //@ValidPassword(message = "Password phải >=6 ký tự, có chữ hoa và số")
    private String password;
}
