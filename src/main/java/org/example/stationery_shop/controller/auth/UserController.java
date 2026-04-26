package org.example.stationery_shop.controller.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.request.RegisterRequest;
import org.example.stationery_shop.dto.response.ApiResponse;
import org.example.stationery_shop.entity.auth.User;
import org.example.stationery_shop.service.UserService;
import org.example.stationery_shop.service.serviceImpl.UserServiceImpl;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
