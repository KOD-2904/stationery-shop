package org.example.stationery_shop.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {
    private final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/login", "/auth/token", "/auth/introspect", "/auth/log-out", "/auth/refreshToken", "/api/auth/register", "/auth/verify-email"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpRequest) throws Exception {
        httpRequest
                .cors(cors -> {})
                .csrf(AbstractHttpConfigurer::disable)
                //.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(authorizeRequests -> {
                    authorizeRequests
                            .requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll()
                            .requestMatchers(HttpMethod.GET, "/auth/verify-email").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/payment/**").permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/payment/**").permitAll()
//                            .requestMatchers(HttpMethod.POST, "/register").permitAll()
//                            .requestMatchers(HttpMethod.POST, "/auth/introspect").permitAll()
//                            .requestMatchers(HttpMethod.POST, "/auth/log-out").permitAll()
//                            .requestMatchers(HttpMethod.GET, "/users").hasAuthority("ROLE_ADMIN")
                            .anyRequest().authenticated();
                });
//                .oauth2ResourceServer(oauth2 -> oauth2
//                        .jwt(jwt -> jwt
//                                .jwtAuthenticationConverter(jwtConverter())
//                        )
//                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
//                )
//                .exceptionHandling(exceptions -> exceptions
//                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint()));
        httpRequest.csrf(AbstractHttpConfigurer::disable);
        return httpRequest.build();
    }
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
