package org.example.stationery_shop.dto.request;

import lombok.*;
import org.example.stationery_shop.validation.email.ValidEmail;
import org.example.stationery_shop.validation.password.ValidPassword;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenRequest {
    private String email;
}
