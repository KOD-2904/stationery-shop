package org.example.stationery_shop.controller.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.request.LoginRequest;
import org.example.stationery_shop.dto.response.ApiResponse;
import org.example.stationery_shop.dto.response.AuthResponse;
import org.example.stationery_shop.entity.auth.User;
import org.example.stationery_shop.service.auth.AuthService;
import org.example.stationery_shop.service.serviceImpl.auth.AuthServiceImpl;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/")
public class AuthController {
    private final AuthServiceImpl authService;

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest httpServletRequest) {
        authService.login(loginRequest, httpServletRequest.getRemoteAddr());

        return ApiResponse.<AuthResponse>builder()
                .code(200)
                .message("Login successful")
                .result(authService.login(loginRequest, httpServletRequest.getRemoteAddr()))
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
}
