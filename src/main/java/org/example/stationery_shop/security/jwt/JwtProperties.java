package org.example.stationery_shop.security.jwt;

import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
//@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    @Value("${jwt.secret}")
    private String secret;
    private  int accessTokenExpiration = 10*60; //? ngày
    private  int refreshTokenExpiration = 60*60*24*7;
}
