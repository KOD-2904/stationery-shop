package org.example.stationery_shop.mapper;

import org.example.stationery_shop.dto.response.UserResponse;
import org.example.stationery_shop.entity.auth.Role;
import org.example.stationery_shop.entity.auth.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {
    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }

        Set<String> providers = user.getProviders() == null
                ? Collections.emptySet()
                : Set.copyOf(user.getProviders());

        Set<String> roles = user.getRoles() == null
                ? Collections.emptySet()
                : user.getRoles().stream()
                .map(Role::getCode)
                .collect(Collectors.toSet());

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .phoneVerified(user.isPhoneVerified())
                .status(user.getStatus())
                .providers(providers)
                .roles(roles)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
