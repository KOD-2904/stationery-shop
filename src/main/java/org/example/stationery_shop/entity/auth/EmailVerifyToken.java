package org.example.stationery_shop.entity.auth;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.stationery_shop.entity.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_verify_token", indexes = {
        @Index(name = "idx_evt_token", columnList = "token", unique = true),
        @Index(name = "idx_evt_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class EmailVerifyToken extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    String token;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(name = "expires_at", nullable = false)
    LocalDateTime expiresAt;

    @Column(name = "used", nullable = false)
    boolean used = false;

    @Column(name = "used_at")
    LocalDateTime usedAt;
}
