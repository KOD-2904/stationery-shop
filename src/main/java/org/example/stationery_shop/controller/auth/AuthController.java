package org.example.stationery_shop.controller.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.request.LoginRequest;
import org.example.stationery_shop.dto.response.ApiResponse;
import org.example.stationery_shop.dto.response.AuthResponse;
import org.example.stationery_shop.exception.AppException;
import org.example.stationery_shop.exception.ErrorCode;
import org.example.stationery_shop.security.jwt.JwtProperties;
import org.example.stationery_shop.service.serviceImpl.auth.AuthServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/")
public class AuthController {
    private final AuthServiceImpl authService;
    private final JwtProperties jwtProperties;

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        String device = "UNKNOWN_DEVICE";

        AuthResponse result = authService.login(loginRequest, ip, device, userAgent);
        setTokenCookies(response, result.getAccessToken(), result.getRefreshToken());

        return ApiResponse.<AuthResponse>builder()
                .code(200)
                .message("Login successful")
                .result(result)
                .build();
    }

    @PostMapping("/login-google")
    public ApiResponse<AuthResponse> loginGoogle() {
        return ApiResponse.<AuthResponse>builder()
                .code(200)
                .message("Login successful")
                .build();
    }

    @PostMapping("/set-password")
    public ApiResponse<AuthResponse> setPasswordForGoogleUser() {
        return ApiResponse.<AuthResponse>builder()
                .code(200)
                .message("")
                .build();
    }

    @PostMapping("/refreshToken")
    public ApiResponse<AuthResponse> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = getRefreshTokenFromRequest(request);
        if (refreshToken == null) {
            throw new AppException(ErrorCode.TOKEN_NOT_FOUND);
        }

        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        String device = "UNKNOWN_DEVICE";

        AuthResponse result = authService.refreshToken(refreshToken, ip, userAgent, device);
        setTokenCookies(response, result.getAccessToken(), result.getRefreshToken());

        return ApiResponse.<AuthResponse>builder()
                .code(200)
                .message("Refresh token successful")
                .result(result)
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logoutOneDevice(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        try {
            String refreshToken = getRefreshTokenFromRequest(request);
            if (refreshToken != null) {
                authService.logoutOneDevice(refreshToken);
            }
        } catch (Exception ignored) {
            // Logout should still clear browser cookies when the Redis session is already gone.
        } finally {
            clearTokenCookies(response);
        }

        return ApiResponse.<Void>builder()
                .code(200)
                .message("Logout successful")
                .build();
    }

    @PostMapping("/logout-all")
    public ApiResponse<Void> logoutAllDevices(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        try {
            String refreshToken = getRefreshTokenFromRequest(request);
            if (refreshToken != null) {
                authService.logoutAllDevices(refreshToken);
            }
        } catch (Exception ignored) {
            // Logout-all should still clear browser cookies when the Redis session is already gone.
        } finally {
            clearTokenCookies(response);
        }

        return ApiResponse.<Void>builder()
                .code(200)
                .message("Logout all devices successful")
                .build();
    }

    @GetMapping("/oauth2/success")
    public ApiResponse<?> success() {
        return ApiResponse.builder()
                .code(200)
                .message("Login Google success")
                .result("Login Google success")
                .build();
    }

    private String getRefreshTokenFromRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }

        return null;
    }

    private void setTokenCookies(
            HttpServletResponse response,
            String accessToken,
            String refreshToken
    ) {
        Cookie accessCookie = new Cookie("accessToken", accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(false);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(jwtProperties.getAccessTokenExpiration());

        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(jwtProperties.getRefreshTokenExpiration());

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);
    }

    private void clearTokenCookies(HttpServletResponse response) {
        Cookie accessCookie = new Cookie("accessToken", "");
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(false);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(0);

        Cookie refreshCookie = new Cookie("refreshToken", "");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0);

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);
    }
}
