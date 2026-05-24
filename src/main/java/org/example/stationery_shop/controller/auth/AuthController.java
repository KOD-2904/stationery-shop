package org.example.stationery_shop.controller.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.request.LoginRequest;
import org.example.stationery_shop.dto.response.ApiResponse;
import org.example.stationery_shop.dto.response.AuthResponse;
import org.example.stationery_shop.entity.auth.User;
import org.example.stationery_shop.exception.AppException;
import org.example.stationery_shop.exception.ErrorCode;
import org.example.stationery_shop.security.jwt.JwtProperties;
import org.example.stationery_shop.service.auth.AuthService;
import org.example.stationery_shop.service.serviceImpl.auth.AuthServiceImpl;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/")
public class AuthController {
    private final AuthServiceImpl authService;
    private final JwtProperties jwtProperties;

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse
    ) {
        String ip = httpServletRequest.getRemoteAddr();
        String userAgent = httpServletRequest.getHeader("User-Agent");

        String device = "UNKNOWN_DEVICE";
        // Hoặc tự parse ra tên thiết bị sau cũng được
        var result = authService.login(loginRequest, ip, userAgent, device);
        setTokenCookies(httpServletResponse, result.getAccessToken(), result.getRefreshToken());
        return ApiResponse.<AuthResponse>builder()
                .code(200)
                .message("Login successful")
                .result(result)
                .build();
    }
    @PostMapping("/login-google") //user loginbang google
    public ApiResponse<AuthResponse> loginGoogle(
            ) {
        return ApiResponse.<AuthResponse>builder()
                .code(200)
                .message("Login successful")
            //    .result(authService.login(loginRequest, httpServletRequest.getRemoteAddr()))
                .build();
    }
    @PostMapping("/set-password") //set thêm password cho user truoc do login bang google
    public ApiResponse<AuthResponse> setPasswordForGoogleUser(
            ) {
        return ApiResponse.<AuthResponse>builder()
                .code(200)
                .message("")
                //.result(authService.login(loginRequest, httpServletRequest.getRemoteAddr()))
                .build();
    }
//    @PostMapping("/logout")
//    public ResponseEntity<?> logout(HttpServletResponse response) {
//        // Xóa cookies bằng cách set maxAge = 0
//        Cookie accessCookie = new Cookie("accessToken", "");
//        accessCookie.setHttpOnly(true);
//        accessCookie.setPath("/");
//        accessCookie.setMaxAge(0);
//
//        Cookie refreshCookie = new Cookie("refreshToken", "");
//        refreshCookie.setHttpOnly(true);
//        refreshCookie.setPath("/");
//        refreshCookie.setMaxAge(0);
//
//        response.addCookie(accessCookie);
//        response.addCookie(refreshCookie);
//
//        return ResponseEntity.ok(Map.of("message", "Logged out"));
//    }
    @PostMapping("/refreshToken")
    public ApiResponse<AuthResponse> refreshToken(HttpServletRequest httpServletRequest,
                                                  HttpServletResponse httpServletResponse) {
        String ip = httpServletRequest.getRemoteAddr();
        String userAgent = httpServletRequest.getHeader("User-Agent");

        String device = "UNKNOWN_DEVICE";

        String refreshToken = getRefreshTokenFromCookie(httpServletRequest);
        // Hoặc tự parse ra tên thiết bị sau cũng được
        var result = authService.refreshToken(refreshToken, ip, userAgent, device);

        setTokenCookies(httpServletResponse, result.getAccessToken(), result.getRefreshToken());

        if (refreshToken == null) {
            throw new AppException(ErrorCode.TOKEN_NOT_FOUND);
        }
        return ApiResponse.<AuthResponse>builder()
                .result(result)
                .build();
    }
    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
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
    @GetMapping("/oauth2/success")
    public ApiResponse<?> success() {
        return ApiResponse.builder()
                .result("Login Google success")
                .build();
    }
}
