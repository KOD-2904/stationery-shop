package org.example.stationery_shop.service.serviceImpl.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.stationery_shop.dto.request.LoginRequest;
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
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

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
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
        } catch (DisabledException e) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_ACTIVE);
        } catch (LockedException e) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        } catch (AuthenticationException e) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        if (userDetails == null) {
            throw new AppException(ErrorCode.USER_DETAILS_IS_NULL);
        }

        String email = userDetails.getUsername();

        User user = userRepository.findWithRolesByEmail(email).orElseThrow(
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

        redisService.logoutOneDevice(userId, oldJti);

        User user = userRepository.findWithRolesById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));
        if (user.getStatus() == UserStatus.BANNED) {
            redisService.logoutAllDevices(user.getId());
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        }

        String newAccessToken = jwtService.generateAccessToken(user);
        JwtTokenResult jwtTokenResult = jwtService.generateRefreshTokenResult(user);

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
    public void logoutOneDevice(String refreshToken) {
        var claims = jwtService.parseToken(refreshToken).getBody();
        String jti = claims.getId();
        String userId = claims.get("userId", String.class);

        RefreshTokenSession session = redisService.getSession(jti);
        if (session == null) {
            throw new AppException(ErrorCode.TOKEN_NOT_FOUND);
        }

        redisService.logoutOneDevice(userId, jti);
    }

    @Override
    public void logoutAllDevices(String refreshToken) {
        var claims = jwtService.parseToken(refreshToken).getBody();
        String jti = claims.getId();
        String userId = claims.get("userId", String.class);

        RefreshTokenSession session = redisService.getSession(jti);
        if (session == null) {
            throw new AppException(ErrorCode.TOKEN_NOT_FOUND);
        }

        redisService.logoutAllDevices(userId);
    }
}
