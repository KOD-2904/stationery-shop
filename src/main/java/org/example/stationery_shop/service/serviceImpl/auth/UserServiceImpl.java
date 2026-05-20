package org.example.stationery_shop.service.serviceImpl.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.stationery_shop.dto.request.RegisterRequest;
import org.example.stationery_shop.entity.auth.Role;
import org.example.stationery_shop.entity.auth.User;
import org.example.stationery_shop.enums.UserStatus;
import org.example.stationery_shop.exception.AppException;
import org.example.stationery_shop.exception.ErrorCode;
import org.example.stationery_shop.repository.MailRepository;
import org.example.stationery_shop.repository.RoleRepository;
import org.example.stationery_shop.repository.UserRepository;
import org.example.stationery_shop.service.auth.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private static final String baseUrl = "http://localhost:8080";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailServiceImpl mailService;
    private final MailRepository mailRepository;
    @Override
    public User register(RegisterRequest registerRequest) {

        Optional<User> optionalUser =
                userRepository.findByEmail(registerRequest.getEmail());

        User user;

        // =========================================
        // EMAIL ĐÃ TỒN TẠI
        // =========================================
        if(optionalUser.isPresent()) {

            user = optionalUser.get();

            Set<String> providers = user.getProviders();

            // -------------------------------------
            // Đã có local rồi
            // -------------------------------------
            if(providers.contains("local")) {
                throw new AppException(ErrorCode.EMAIL_EXISTED);
            }

            // -------------------------------------
            // Chỉ có google
            // => add local
            // -------------------------------------
            providers.add("local");

            user.setProviders(providers);

            user.setPassword(
                    passwordEncoder.encode(registerRequest.getPassword())
            );

            // nếu muốn verify email lại
            // user.setStatus(UserStatus.INACTIVE);

            userRepository.save(user);

            return user;
        }

        // =========================================
        // EMAIL CHƯA TỒN TẠI
        // =========================================

        HashSet<Role> roles = new HashSet<>();

        Role roleUser = (Role) roleRepository.findByCode("ROLE_USER")
                .orElseThrow(() ->
                        new AppException(ErrorCode.ROLE_NOT_EXIST));

        roles.add(roleUser);

        user = User.builder()
                .roles(roles)
                .email(registerRequest.getEmail())
                .password(
                        passwordEncoder.encode(registerRequest.getPassword())
                )
                .providers(new HashSet<>(Set.of("local")))
                .status(UserStatus.INACTIVE)
                .build();

        User returnUser = userRepository.save(user);

        var emailVerifyToken =
                mailService.buildEmailVerifyToken(returnUser);

        mailRepository.save(emailVerifyToken);

        String link =
                baseUrl +
                        "/auth/verify-user?token=" +
                        emailVerifyToken.getToken();

        mailService.sendMail(
                registerRequest.getEmail(),
                link
        );

        return returnUser;
    }
//    @Override
//    public User register(RegisterRequest registerRequest) {
//        if (userRepository.existsByEmail(registerRequest.getEmail())) {
//            throw new AppException(ErrorCode.EMAIL_EXISTED);
//        }
//
//
//        HashSet<Role> roles = new HashSet<>();
//        Role roleUser = (Role) roleRepository.findByCode("ROLE_USER")
//                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXIST));
//        roles.add(roleUser);
//        User user = User.builder()
//                .roles(roles)
//                .email(registerRequest.getEmail())
//                .password(passwordEncoder.encode(registerRequest.getPassword()))
//                //.provider("local")
//                .providers(new HashSet<>(Set.of("local")))
//                .status(UserStatus.INACTIVE)//tamjhtowif chưa, verify bằng email
//                .build();
//
//       var returnUser = userRepository.save(user);
//       var emailVerifyToken = mailService.buildEmailVerifyToken(returnUser);
//
//        mailRepository.save(emailVerifyToken);
//        String link = baseUrl + "/auth/verify-user?token=" + emailVerifyToken.getToken();
//
//        mailService.sendMail(registerRequest.getEmail(), link);
//        return user;
//    }

    @Override
    public void verifyUser(String token) {
        mailService.verifyMail(token);
    }

    @Override
    public void resendVerifyToken(User user) {
        var emailVerifyToken =  mailService.buildEmailVerifyToken(user);
        mailRepository.save(emailVerifyToken);
        String link = baseUrl + "/auth/verify-user?token=" + emailVerifyToken.getToken();
        mailService.sendMail(user.getEmail(), link);
    }


}
