package org.example.stationery_shop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.stationery_shop.enums.UserStatus;

import java.time.Instant;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String email;
    private String name;
    private String phone;
    private boolean phoneVerified;
    private UserStatus status;
    private Set<String> providers;
    private Set<String> roles;
    private Instant createdAt;
    private Instant updatedAt;
}
