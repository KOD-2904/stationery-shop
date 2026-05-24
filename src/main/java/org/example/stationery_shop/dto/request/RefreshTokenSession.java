package org.example.stationery_shop.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenSession {
    private String jti;
    private String userId;
    private String device;
    private String ip;
    private String userAgent;
    private Instant loginAt;
}

