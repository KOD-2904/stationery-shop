package org.example.stationery_shop.service;

import org.example.stationery_shop.dto.response.UserResponse;
import org.example.stationery_shop.dto.response.admin.AdminDashboardResponse;
import org.example.stationery_shop.enums.UserStatus;

import java.util.List;

public interface AdminService {
    AdminDashboardResponse getDashboard();
    List<UserResponse> getUsers(UserStatus status);
    UserResponse updateUserStatus(String userId, UserStatus status);
    UserResponse updateUserRole(String userId, String roleCode, boolean enabled);
    void logoutAllUserDevices(String userId);
    void logoutAllUsers();
}
