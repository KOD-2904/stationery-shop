package org.example.stationery_shop.service.serviceImpl.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.example.stationery_shop.dto.request.LoginRequest;
import org.example.stationery_shop.dto.request.LogoutRequest;
import org.example.stationery_shop.dto.request.RefreshTokenRequest;
import org.example.stationery_shop.dto.response.AuthResponse;
import org.example.stationery_shop.entity.auth.User;
import org.example.stationery_shop.exception.AppException;
import org.example.stationery_shop.exception.ErrorCode;
import org.example.stationery_shop.repository.UserRepository;
import org.example.stationery_shop.security.jwt.JwtService;
import org.example.stationery_shop.security.user.CustomUserDetails;
import org.example.stationery_shop.service.auth.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    public AuthResponse login(LoginRequest loginRequest, String deviceIp) {
        try {
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                loginRequest.getEmail(),
                                loginRequest.getPassword()
                        )
                    );
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            assert userDetails != null;
            String email = userDetails.getUsername();

            User user = userRepository.findByEmail(email).orElseThrow(
                    () -> new AppException(ErrorCode.EMAIL_NOT_EXIST)
            );

            String accessToken = jwtService.generateAccessToken(authentication);
            String refreshToken = jwtService.generateRefreshToken(authentication);
            return AuthResponse.builder()
                    .refreshToken(refreshToken)
                    .authenticated(true)
                    .accessToken(accessToken)
                    .build();


        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void refreshToken(RefreshTokenRequest refreshTokenRequest, String deviceIp) {

    }

    @Override
    public void logout(LogoutRequest logoutRequest) {

    }
}
