package org.example.stationery_shop.security.google;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.stationery_shop.entity.auth.User;
import org.example.stationery_shop.enums.UserStatus;
import org.example.stationery_shop.repository.UserRepository;
import org.example.stationery_shop.security.jwt.JwtService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final JwtService jwtService;
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

        Optional<User> optionalUser = userRepository.findByEmail(email);

        User user;

        if(optionalUser.isPresent()) {

            user = optionalUser.get();

            // Nếu account local chưa link google
            if(!user.getProviders().contains("google")) {
                user.getProviders().add("google");
            }

            // Nếu chưa có googleId thì add
            if(user.getGoogleId() == null) {
                user.setGoogleId(googleId);
            }

            // update name nếu muốn
            if(user.getName() == null || user.getName().isBlank()) {
                user.setName(name);
            }

            userRepository.save(user);

        } else {

            user = User.builder()
                    .email(email)
                    .name(name)
                    .googleId(googleId)
                    .status(UserStatus.ACTIVE)
                    .password(null)
                    .providers(new HashSet<>(Set.of("google")))
                    .build();

            user = userRepository.save(user);
        }
        log.info("Name : {}", name);
        log.info("sub : {}", googleId);
        log.info("email : {}", email);

//        String token = jwtService.generateAccessToken();
//        response.sendRedirect(
//                "http://localhost:3000/oauth2/success?token=" + token
//        );

    }
}
