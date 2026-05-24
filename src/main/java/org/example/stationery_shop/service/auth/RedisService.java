package org.example.stationery_shop.service.auth;

import org.example.stationery_shop.dto.request.RefreshTokenSession;

import java.time.Duration;

public interface RedisService {
    void saveRefreshTokenSession(String userId,
                                 String jti,
                                 String device,
                                 String ip,
                                 String userAgent);
    RefreshTokenSession getSession(String jti);
    boolean isRefreshTokenValid(String jti);
    void logoutOneDevice(String userId, String jti);
    void logoutAllDevices(String userId);
    void getAllSessions(String userId);
}
