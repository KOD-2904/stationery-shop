package org.example.stationery_shop.service.auth;

import org.example.stationery_shop.dto.request.LoginRequest;
import org.example.stationery_shop.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest loginRequest, String ip, String device, String userAgent);
    AuthResponse refreshToken(String token, String ip, String userAgent, String deviceIp);
    void logoutOneDevice(String refreshToken);
    void logoutAllDevices(String refreshToken);
}
