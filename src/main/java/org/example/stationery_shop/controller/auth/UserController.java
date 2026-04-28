package org.example.stationery_shop.controller.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.request.RegisterRequest;
import org.example.stationery_shop.dto.response.ApiResponse;
import org.example.stationery_shop.entity.auth.User;

import org.example.stationery_shop.service.serviceImpl.auth.UserServiceImpl;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/")
@RequiredArgsConstructor
public class UserController {
    private final UserServiceImpl userService;

    @PostMapping("/register")
    public ApiResponse<User> register(
            @Valid @RequestBody RegisterRequest registerRequest
    ) {
        User user = userService.register(registerRequest);
        return ApiResponse.<User>builder()
                .code(200)
                .message("Registered Successfully")
                .result(user)
                .build();
        //return ApiResponse.success("Registered Successfully",userResponse);
    }

    @PostMapping("/verify-user")
    public ApiResponse verifyUser(@RequestParam String token) {
        userService.verifyUser(token);
        return ApiResponse.builder()
                .code(200)
                .message("Verify Successfully, your account has been verified")
                .build();
        //return ApiResponse.success("Registered Successfully",userResponse);
    }

    @GetMapping("/resend-verify-user")
    public ApiResponse resendVerifyToken(
            @AuthenticationPrincipal(expression = "user") User user) {
        userService.resendVerifyToken(user);
        return ApiResponse.builder()
                .code(200)
                .message("Verify Successfully, your account has been verified")
                .build();
    }
}
