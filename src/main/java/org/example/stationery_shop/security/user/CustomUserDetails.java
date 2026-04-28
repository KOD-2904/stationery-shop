package org.example.stationery_shop.security.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.entity.auth.Permission;
import org.example.stationery_shop.entity.auth.Role;
import org.example.stationery_shop.entity.auth.User;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();

        if (user.getRoles() != null) {
            for (Role role : user.getRoles()) {
                // Thêm role với prefix ROLE_ (để dùng hasRole())
                authorities.add(new SimpleGrantedAuthority(
                        //"ROLE_" +  đoạn này lúc vừa khởi chạy app sẽ tạo trước  ROlE là
                        // ROLE_ADMIN, ROLE_STAFF, ROLE_USER nên có thể chưa cần  prefix
                                role.getCode()));

                // Thêm permissions (không prefix, để dùng hasAuthority())
                if (role.getPermissions() != null) {
                    for (Permission permission : role.getPermissions()) {
                        authorities.add(new SimpleGrantedAuthority(permission.getCode()));
                    }
                }
            }
        }

        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword(); // Trả về password đã encode
    }

    @Override
    public String getUsername() {
        return user.getEmail(); // Dùng email làm username
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Hoặc lấy từ user nếu có field tương ứng
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Hoặc lấy từ user.getNonLocked()
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true; // Giả sử User có field enabled //doan nay van can sua sau
    }
}
