package org.example.stationery_shop.security.jwt;

import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.Duration;

@Data
@Component
//@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    @Value("${jwt.secret}")
    private String secret;
    private  int accessTokenExpiration = 10*60; //
    private int refreshTokenExpiration = 60 * 60 * 24 * 7;

    public long getAccessTokenExpirationMillis() {
        return Duration.ofSeconds(accessTokenExpiration).toMillis();
    }

    public long getRefreshTokenExpirationMillis() {
        return Duration.ofSeconds(refreshTokenExpiration).toMillis();
    }

    public Duration getRefreshTokenDuration() {
        return Duration.ofSeconds(refreshTokenExpiration);
    }
}
