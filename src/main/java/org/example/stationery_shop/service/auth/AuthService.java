package org.example.stationery_shop.service.auth;

import org.example.stationery_shop.dto.request.LoginRequest;
import org.example.stationery_shop.dto.request.LogoutRequest;
import org.example.stationery_shop.dto.request.RefreshTokenRequest;
import org.example.stationery_shop.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest loginRequest, String deviceIp);
    void refreshToken(RefreshTokenRequest refreshTokenRequest, String deviceIp);
    void logout(LogoutRequest logoutRequest);
}
