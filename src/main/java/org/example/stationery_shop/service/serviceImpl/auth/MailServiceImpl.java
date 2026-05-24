package org.example.stationery_shop.service.serviceImpl.auth;

import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.entity.auth.EmailVerifyToken;
import org.example.stationery_shop.entity.auth.User;
import org.example.stationery_shop.enums.UserStatus;
import org.example.stationery_shop.exception.AppException;
import org.example.stationery_shop.exception.ErrorCode;
import org.example.stationery_shop.repository.MailRepository;
import org.example.stationery_shop.repository.UserRepository;
import org.example.stationery_shop.service.auth.MailService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {
    private final JavaMailSender mailSender;
    private final MailRepository mailRepository;
    private final UserRepository userRepository;

    @Override
    public void sendMail(String to, String verifyLink) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Xác nhận đăng ký tài khoản");
        msg.setText("""
                Chào bạn,
                
                Vui lòng bấm link sau để xác nhận email:
                %s
                
                Link sẽ hết hạn sau một thời gian.
                """.formatted(verifyLink));

        mailSender.send(msg);
    }

    @Override
    public void  verifyMail(String token) {
        EmailVerifyToken evt = mailRepository.findByToken(token)
                .orElseThrow(() -> new AppException(ErrorCode.TOKEN_NOT_FOUND));
//        if (evt.isUsed()) {
//            throw new AppException(ErrorCode.TOKEN_ALREADY_USED);
//        }
        if (evt.getExpiresAt().isBefore(Instant.now())) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }

        User user = evt.getUser();
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        mailRepository.deleteAllByUser(user);
    }
    public EmailVerifyToken buildEmailVerifyToken(User user) {
        String token = UUID.randomUUID().toString();
        return EmailVerifyToken.builder()
                .token(token)
                .user(user)
                .expiresAt(Instant.now().plus(Duration.ofMinutes(30)))
                .used(false)
                .build();
    }
}

