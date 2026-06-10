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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/oauth2/**", "/api/auth/login", "/api/auth/token", "/api/auth/introspect", "/api/auth/log-out", "/api/auth/logout", "/api/auth/logout-all", "/api/auth/refreshToken", "/api/auth/register", "/api/auth/verify-email", "/api/auth/verify-user", "/api/auth/forgot-password/send-otp", "/api/auth/forgot-password/reset"
    };
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtAuthenticationEntryPoint authenticationFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final FrontendProperties frontendProperties;
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpRequest) throws Exception {
        httpRequest
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
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
                            .requestMatchers(HttpMethod.GET, "/api/ghn/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/payment/vnpay-callback").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/payment/vnpay-ipn").permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/payment/vnpay-ipn").permitAll()
                            .anyRequest().authenticated();
                })

                .oauth2Login(oAuth2Login -> oAuth2Login
                        .successHandler(oAuth2SuccessHandler))
        ;
      httpRequest.csrf(AbstractHttpConfigurer::disable);
        return httpRequest.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        Set<String> allowedOriginPatterns = new LinkedHashSet<>();
        allowedOriginPatterns.add(normalizeOrigin(frontendProperties.getBaseUrl()));
        frontendProperties.getAllowedOrigins().stream()
                .filter(origin -> origin != null && !origin.isBlank())
                .map(this::normalizeOrigin)
                .forEach(allowedOriginPatterns::add);

        configuration.setAllowedOriginPatterns(new ArrayList<>(allowedOriginPatterns));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private String normalizeOrigin(String url) {
        return url.replaceAll("/+$", "");
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
