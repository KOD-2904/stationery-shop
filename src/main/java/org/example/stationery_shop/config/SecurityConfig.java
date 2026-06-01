package org.example.stationery_shop.config;

import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.security.google.OAuth2SuccessHandler;
import org.example.stationery_shop.security.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/oauth2/**", "/api/auth/login", "/api/auth/token", "/api/auth/introspect", "/api/auth/log-out", "/api/auth/logout", "/api/auth/logout-all", "/api/auth/refreshToken", "/api/auth/register", "/api/auth/verify-email", "/api/auth/verify-user"
    };
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtAuthenticationEntryPoint authenticationFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpRequest) throws Exception {
        httpRequest
                .cors(cors -> {})
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptions ->
                        exceptions.authenticationEntryPoint(authenticationFilter)
                )
                .authorizeHttpRequests(authorizeRequests -> {
                    authorizeRequests
                            .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                            .requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll()
                            .requestMatchers(HttpMethod.GET, PUBLIC_ENDPOINTS).permitAll()
                            .requestMatchers("/error").permitAll()
                            .requestMatchers(HttpMethod.GET, "/auth/verify-email").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/brands/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/product-variants/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/stores/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/inventory/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/payments/vnpay-return").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/payments/vnpay-ipn").permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/payments/vnpay-ipn").permitAll()
                            .anyRequest().authenticated();
                })

                .oauth2Login(oAuth2Login -> oAuth2Login
                        .successHandler(oAuth2SuccessHandler))
        ;
      httpRequest.csrf(AbstractHttpConfigurer::disable);
        return httpRequest.build();
    }
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
