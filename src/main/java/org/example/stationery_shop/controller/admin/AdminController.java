package org.example.stationery_shop.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.request.carrier.CarrierRequest;
import org.example.stationery_shop.dto.request.admin.UpdateUserRoleRequest;
import org.example.stationery_shop.dto.request.admin.UpdateUserStatusRequest;
import org.example.stationery_shop.dto.response.ApiResponse;
import org.example.stationery_shop.dto.response.UserResponse;
import org.example.stationery_shop.dto.response.admin.AdminDashboardResponse;
import org.example.stationery_shop.dto.response.carrier.CarrierResponse;
import org.example.stationery_shop.enums.UserStatus;
import org.example.stationery_shop.service.AdminService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ApiResponse<AdminDashboardResponse> getDashboard() {
        return ApiResponse.<AdminDashboardResponse>builder()
                .code(200)
                .message("Success")
                .result(adminService.getDashboard())
                .build();
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<List<UserResponse>> getUsers(@RequestParam(required = false) UserStatus status) {
        return ApiResponse.<List<UserResponse>>builder()
                .code(200)
                .message("Success")
                .result(adminService.getUsers(status))
                .build();
    }

    @PatchMapping("/users/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<UserResponse> updateUserStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        return ApiResponse.<UserResponse>builder()
                .code(200)
                .message("Updated user status successfully")
                .result(adminService.updateUserStatus(id, request.getStatus()))
                .build();
    }

    @PatchMapping("/users/{id}/roles")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<UserResponse> updateUserRole(
            @PathVariable String id,
            @Valid @RequestBody UpdateUserRoleRequest request
    ) {
        return ApiResponse.<UserResponse>builder()
                .code(200)
                .message("Updated user role successfully")
                .result(adminService.updateUserRole(id, request.getRoleCode(), request.getEnabled()))
                .build();
    }

    @PostMapping("/users/{id}/logout-all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<Void> logoutAllUserDevices(@PathVariable String id) {
        adminService.logoutAllUserDevices(id);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Logged out all user devices successfully")
                .build();
    }

    @PostMapping("/users/logout-all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<Void> logoutAllUsers() {
        adminService.logoutAllUsers();
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Logged out all users successfully")
                .build();
    }

    @GetMapping("/carriers")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<List<CarrierResponse>> getCarriers() {
        return ApiResponse.<List<CarrierResponse>>builder()
                .code(200)
                .message("Success")
                .result(adminService.getCarriers())
                .build();
    }

    @PostMapping("/carriers")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<CarrierResponse> createCarrier(@Valid @RequestBody CarrierRequest request) {
        return ApiResponse.<CarrierResponse>builder()
                .code(200)
                .message("Created carrier successfully")
                .result(adminService.createCarrier(request))
                .build();
    }

    @PutMapping("/carriers/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<CarrierResponse> updateCarrier(@PathVariable String id, @Valid @RequestBody CarrierRequest request) {
        return ApiResponse.<CarrierResponse>builder()
                .code(200)
                .message("Updated carrier successfully")
                .result(adminService.updateCarrier(id, request))
                .build();
    }

    @PatchMapping("/carriers/{id}/active")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<CarrierResponse> updateCarrierActive(@PathVariable String id, @RequestParam boolean active) {
        return ApiResponse.<CarrierResponse>builder()
                .code(200)
                .message("Updated carrier status successfully")
                .result(adminService.updateCarrierActive(id, active))
                .build();
    }
}
