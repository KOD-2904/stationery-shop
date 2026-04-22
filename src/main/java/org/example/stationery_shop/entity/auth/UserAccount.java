package org.example.stationery_shop.entity.auth;

import jakarta.persistence.*;
import lombok.*;
import org.example.stationery_shop.entity.BaseEntity;
import org.example.stationery_shop.enums.UserStatus;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_account",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_user_phone", columnNames = "phone")
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserAccount extends BaseEntity {

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "password", nullable = true, length = 255) //null with Google login
    private String password;

    @Column(name = "firstname", length = 255)
    private String name; // get from google

    @Column(name = "phone", length = 20)
    private String phone;

    // Quản lý đăng nhập
    @Column(unique = true)
    private String googleId;           // ID từ Google (để biết user này từ Google)

    private String provider;           // "google" hoặc "local"

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status = UserStatus.INACTIVE;

    @Column(name = "is_phone_verified", nullable = false)
    private boolean phoneVerified = false;

    private LocalDateTime lastLoginAt;
}
