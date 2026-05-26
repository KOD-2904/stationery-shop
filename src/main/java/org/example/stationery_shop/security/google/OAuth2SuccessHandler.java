package org.example.stationery_shop.security.google;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.stationery_shop.entity.auth.Role;
import org.example.stationery_shop.entity.auth.User;
import org.example.stationery_shop.enums.UserStatus;
import org.example.stationery_shop.exception.AppException;
import org.example.stationery_shop.exception.ErrorCode;
import org.example.stationery_shop.repository.RoleRepository;
import org.example.stationery_shop.repository.UserRepository;
import org.example.stationery_shop.security.jwt.JwtProperties;
import org.example.stationery_shop.security.jwt.JwtService;
import org.example.stationery_shop.security.jwt.JwtTokenResult;
import org.example.stationery_shop.service.serviceImpl.auth.RedisServiceImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final RedisServiceImpl redisService;
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        String googleId = oauthUser.getAttribute("sub");
        String name = oauthUser.getAttribute("name");

        Optional<User> optionalUser = userRepository.findWithRolesByEmail(email);

        User user;

        if(optionalUser.isPresent()) {

            user = optionalUser.get();

            if (user.getProviders() == null) {
                user.setProviders(new HashSet<>());
            }

            user.getProviders().add("google");

            // Nếu chưa có googleId thì add
            if(user.getGoogleId() == null) {
                user.setGoogleId(googleId);
            }

            // update name nếu muốn
            if(user.getName() == null || user.getName().isBlank()) {
                user.setName(name);
            }
            user.setLastLoginAt(Instant.now());

            user = userRepository.save(user);

        } else {
            HashSet<Role> roles = new HashSet<>();

            Role roleUser = roleRepository.findByCode("ROLE_USER")
                    .orElseThrow(() ->
                            new AppException(ErrorCode.ROLE_NOT_EXIST));

            roles.add(roleUser);

            user = User.builder()
                    .email(email)
                    .name(name)
                    .googleId(googleId)
                    .roles(roles)
                    .status(UserStatus.ACTIVE)
                    .password(null)
                    .providers(new HashSet<>(Set.of("google")))
                    .lastLoginAt(Instant.now())
                    .build();

            user = userRepository.save(user);
        }
        user = userRepository.findWithRolesById(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));
        log.info("Name : {}", name);
        log.info("sub : {}", googleId);
        log.info("email : {}", email);

        String accessToken = jwtService.generateAccessToken(user);
        JwtTokenResult jwtTokenResult = jwtService.generateRefreshTokenResult(user);
        String refreshToken = jwtTokenResult.getToken();

        redisService.saveRefreshTokenSession(
                user.getId(),
                jwtTokenResult.getJti(),
                "OAUTH2_BROWSER",
                request.getRemoteAddr(),
                request.getHeader("User-Agent")
        );


        // Access Token Cookie (15 phút)
        Cookie accessCookie = new Cookie("accessToken", accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(false); // true cho production
        accessCookie.setPath("/");
        accessCookie.setMaxAge(jwtProperties.getAccessTokenExpiration());

        // Refresh Token Cookie (7 ngày)
        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(jwtProperties.getRefreshTokenExpiration());

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        response.sendRedirect("/api/auth/oauth2/success");
    }
}
