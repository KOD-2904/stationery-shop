package org.example.stationery_shop.entity.auth;

import jakarta.persistence.*;
import lombok.*;
import org.example.stationery_shop.entity.BaseEntity;
import org.example.stationery_shop.enums.UserStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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
@Builder
public class User extends BaseEntity {

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "password", nullable = true, length = 255) //null with Google login
    private String password;

    @Column(name = "name", length = 255)
    private String name; // get from google

    @Column(name = "phone", length = 20)
    private String phone;

    // Quản lý đăng nhập
    @Column(unique = true)
    private String googleId;           // ID từ Google (để biết user này từ Google)

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_provider",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "provider")
    private Set<String> providers = new HashSet<>();         // "google" hoặc "local"
//    @ElementCollection(fetch = FetchType.EAGER)   //nen can nhac
//    private Set<String> providers = new HashSet<>();
//    private String provider;


    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status = UserStatus.INACTIVE;

    @Column(name = "is_phone_verified", nullable = false)
    private boolean phoneVerified = false;

    private Instant lastLoginAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"),
            uniqueConstraints = {
                    @UniqueConstraint(name = "pk_user_role", columnNames = {"user_id", "role_id"})
            }
    )
    private Set<Role> roles = new HashSet<>();
}
