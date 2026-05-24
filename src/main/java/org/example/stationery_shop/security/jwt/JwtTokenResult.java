package org.example.stationery_shop.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtTokenResult {
    private String token;
    private String jti;
    private String userId;
    private Date issuedAt;
    private Date expiredAt;
}

