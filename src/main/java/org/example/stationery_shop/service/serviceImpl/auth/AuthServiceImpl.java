package org.example.stationery_shop.service.serviceImpl.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.example.stationery_shop.dto.request.LoginRequest;
import org.example.stationery_shop.dto.request.LogoutRequest;
import org.example.stationery_shop.dto.request.RefreshTokenRequest;
import org.example.stationery_shop.dto.request.RefreshTokenSession;
import org.example.stationery_shop.dto.response.AuthResponse;
import org.example.stationery_shop.entity.auth.User;
import org.example.stationery_shop.enums.UserStatus;
import org.example.stationery_shop.exception.AppException;
import org.example.stationery_shop.exception.ErrorCode;
import org.example.stationery_shop.repository.UserRepository;
import org.example.stationery_shop.security.jwt.JwtService;
import org.example.stationery_shop.security.jwt.JwtTokenResult;
import org.example.stationery_shop.security.user.CustomUserDetails;
import org.example.stationery_shop.service.auth.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RedisServiceImpl redisService;

    @Override
    public AuthResponse login(LoginRequest loginRequest, String ip, String device, String userAgent) {
        try {
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                loginRequest.getEmail(),
                                loginRequest.getPassword()
                        )
                    );
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            assert userDetails != null;
            String email = userDetails.getUsername();

            User user = userRepository.findByEmail(email).orElseThrow(
                    () -> new AppException(ErrorCode.EMAIL_NOT_EXIST)
            );
            user.setLastLoginAt(Instant.now());
            userRepository.save(user);

            String accessToken = jwtService.generateAccessTokenFromAuthentication(authentication);
            JwtTokenResult refreshTokenResult =
                    jwtService.generateRefreshTokenResultFromAuthentication(authentication);

            redisService.saveRefreshTokenSession(
                    user.getId(),
                    refreshTokenResult.getJti(),
                    device,
                    ip,
                    userAgent
            );

            return AuthResponse.builder()
                    .refreshToken(refreshTokenResult.getToken())
                    .authenticated(true)
                    .accessToken(accessToken)
                    .build();


        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public AuthResponse refreshToken(
            String token,
            String ip,
            String userAgent,
            String device
    ) {
        var claims = jwtService.parseToken(token).getBody();

        String oldJti = claims.getId();
        String userId = claims.get("userId", String.class);

        RefreshTokenSession oldSession = redisService.getSession(oldJti);

        if (oldSession == null) {
            throw new AppException(ErrorCode.TOKEN_NOT_FOUND);
        }

        // Xóa refresh token cũ
        redisService.logoutOneDevice(userId, oldJti);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));
        if (user.getStatus() != UserStatus.ACTIVE) {
            redisService.logoutAllDevices(user.getId());
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        }
        // Gen token mới
        String newAccessToken = jwtService.generateAccessToken(user);
        JwtTokenResult jwtTokenResult = jwtService.generateRefreshTokenResult(user);


        // Lưu refresh token mới vào Redis
        redisService.saveRefreshTokenSession(
                userId,
                jwtTokenResult.getJti(),
                device,
                ip,
                userAgent
        );

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(jwtTokenResult.getToken())
                .authenticated(true)
                .build();
    }

    @Override
    public void logout(LogoutRequest logoutRequest) {

    }
}
