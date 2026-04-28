package org.example.stationery_shop.service.auth;

import org.example.stationery_shop.dto.request.LoginRequest;
import org.example.stationery_shop.dto.request.LogoutRequest;
import org.example.stationery_shop.dto.request.RefreshTokenRequest;

public interface AuthService {
    void login(LoginRequest loginRequest, String deviceIp);
    void refreshToken(RefreshTokenRequest refreshTokenRequest, String deviceIp);
    void logout(LogoutRequest logoutRequest);
}
