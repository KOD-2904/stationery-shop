package org.example.stationery_shop.service.serviceImpl.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.stationery_shop.dto.request.LoginRequest;
import org.example.stationery_shop.dto.request.LogoutRequest;
import org.example.stationery_shop.dto.request.RefreshTokenRequest;
import org.example.stationery_shop.service.auth.AuthService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    @Override
    public void login(LoginRequest loginRequest, String deviceIp) {

    }

    @Override
    public void refreshToken(RefreshTokenRequest refreshTokenRequest, String deviceIp) {

    }

    @Override
    public void logout(LogoutRequest logoutRequest) {

    }
}
