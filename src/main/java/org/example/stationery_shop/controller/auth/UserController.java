package org.example.stationery_shop.controller.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.stationery_shop.dto.request.PhoneOtpSendRequest;
import org.example.stationery_shop.dto.request.PhoneOtpVerifyRequest;
import org.example.stationery_shop.dto.request.RegisterRequest;
import org.example.stationery_shop.dto.request.password.ChangePasswordRequest;
import org.example.stationery_shop.dto.request.password.ForgotPasswordOtpRequest;
import org.example.stationery_shop.dto.request.password.ResetPasswordRequest;
import org.example.stationery_shop.dto.response.ApiResponse;
import org.example.stationery_shop.dto.response.UserResponse;
import org.example.stationery_shop.entity.auth.User;
import org.example.stationery_shop.exception.AppException;
import org.example.stationery_shop.exception.ErrorCode;
import org.example.stationery_shop.mapper.UserMapper;

import org.example.stationery_shop.repository.UserRepository;
import org.example.stationery_shop.service.PasswordOtpService;
import org.example.stationery_shop.service.PhoneVerificationService;
import org.example.stationery_shop.service.serviceImpl.auth.UserServiceImpl;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth/")
@RequiredArgsConstructor
public class UserController {
    private final UserServiceImpl userService;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PhoneVerificationService phoneVerificationService;
    private final PasswordOtpService passwordOtpService;

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(
            @Valid @RequestBody RegisterRequest registerRequest
    ) {
        User user = userService.register(registerRequest);
        return ApiResponse.<UserResponse>builder()
                .code(200)
                .message("Registered Successfully")
                .result(userMapper.toResponse(user))
                .build();
        //return ApiResponse.success("Registered Successfully",userResponse);
    }

    @GetMapping("/verify-user")
    public ApiResponse verifyUserByQuery(@RequestParam String token) {
        userService.verifyUser(token);
        return ApiResponse.builder()
                .code(200)
                .message("Verify Successfully, your account has been verified")
                .build();
    }

//    @PostMapping("/verify-user/{token}")
//    public ApiResponse verifyUser(@PathVariable String token) {
//        userService.verifyUser(token);
//        return ApiResponse.builder()
//                .code(200)
//                .message("Verify Successfully, your account has been verified")
//                .build();
//        //return ApiResponse.success("Registered Successfully",userResponse);
//    }

    @GetMapping("/resend-verify-user")
    public ApiResponse resendVerifyToken(
            Authentication authentication) {
        log.warn("In controller");
        userService.resendVerifyToken(authentication.getName());
        return ApiResponse.builder()
                .code(200)
                .message("Send Successfully")
                .build();
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> me(Authentication authentication) {
        User user = userRepository.findWithRolesByEmail(authentication.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        return ApiResponse.<UserResponse>builder()
                .code(200)
                .message("Current user")
                .result(userMapper.toResponse(user))
                .build();
    }

    @PostMapping("/phone-otp/send")
    public ApiResponse<Void> sendPhoneOtp(@Valid @RequestBody PhoneOtpSendRequest request) {
        phoneVerificationService.sendOtp(request);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("OTP da duoc gui ve email cua ban")
                .build();
    }

    @PostMapping("/phone-otp/verify")
    public ApiResponse<Void> verifyPhoneOtp(@Valid @RequestBody PhoneOtpVerifyRequest request) {
        phoneVerificationService.verifyOtp(request);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Xac thuc so dien thoai thanh cong")
                .build();
    }

    @PostMapping("/forgot-password/send-otp")
    public ApiResponse<Void> sendForgotPasswordOtp(@Valid @RequestBody ForgotPasswordOtpRequest request) {
        passwordOtpService.sendForgotPasswordOtp(request);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("OTP da duoc gui ve email cua ban")
                .build();
    }

    @PostMapping("/forgot-password/reset")
    public ApiResponse<Void> resetForgotPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordOtpService.resetForgotPassword(request);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Dat lai mat khau thanh cong")
                .build();
    }

    @PostMapping("/password/change")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        passwordOtpService.changePassword(request);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Doi mat khau thanh cong")
                .build();
    }
}
