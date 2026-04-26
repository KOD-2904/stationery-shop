package org.example.stationery_shop.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.stationery_shop.dto.request.RegisterRequest;
import org.example.stationery_shop.entity.auth.EmailVerifyToken;
import org.example.stationery_shop.entity.auth.Role;
import org.example.stationery_shop.entity.auth.User;
import org.example.stationery_shop.enums.UserStatus;
import org.example.stationery_shop.exception.AppException;
import org.example.stationery_shop.exception.ErrorCode;
import org.example.stationery_shop.repository.MailRepository;
import org.example.stationery_shop.repository.RoleRepository;
import org.example.stationery_shop.repository.UserRepository;
import org.example.stationery_shop.service.UserService;
import org.springframework.boot.mail.autoconfigure.MailProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private static final String baseUrl = "http://localhost:8080";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailServiceImpl mailService;
    private final MailProperties mailProperties;
    private final MailRepository mailRepository;

    @Override
    public User register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        HashSet<Role> roles = new HashSet<>();
        Role roleUser = (Role) roleRepository.findByCode("ROLE_USER")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXIST));
        roles.add(roleUser);
        User user = User.builder()
                .roles(roles)
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .provider("local")
                .status(UserStatus.INACTIVE)//tamjhtowif chưa, verify bằng email
                .build();

       var returnUser = userRepository.save(user);
        String token = UUID.randomUUID().toString();
        EmailVerifyToken emailVerifyToken = EmailVerifyToken.builder()
                .token(token)
                .user(returnUser)
                .expiresAt(LocalDateTime.now().plus(Duration.ofMinutes(30)))
                .used(false)
                .build();
        mailRepository.save(emailVerifyToken);
        String link = baseUrl + "/auth/verify-email?token=" + token;

        mailService.sendVerifyMail(registerRequest.getEmail(), link);
        return user;
    }
}
