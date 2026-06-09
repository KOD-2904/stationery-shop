package org.example.stationery_shop.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.request.password.ChangePasswordRequest;
import org.example.stationery_shop.dto.request.password.ForgotPasswordOtpRequest;
import org.example.stationery_shop.dto.request.password.ResetPasswordRequest;
import org.example.stationery_shop.entity.auth.User;
import org.example.stationery_shop.exception.AppException;
import org.example.stationery_shop.exception.ErrorCode;
import org.example.stationery_shop.repository.UserRepository;
import org.example.stationery_shop.service.CurrentUserService;
import org.example.stationery_shop.service.PasswordOtpService;
import org.example.stationery_shop.service.auth.MailService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class PasswordOtpServiceImpl implements PasswordOtpService {
    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);
    private static final Duration RATE_WINDOW = Duration.ofMinutes(15);
    private static final int MAX_REQUESTS = 3;

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public void sendForgotPasswordOtp(ForgotPasswordOtpRequest request) {
        User user = userRepository.findByEmail(normalizeEmail(request.getEmail()))
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_EXIST));
        sendOtp(user, "forgot", "Ma OTP dat lai mat khau");
    }

    @Override
    @Transactional
    public void resetForgotPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.PASSWORD_CONFIRM_MISMATCH);
        }
        User user = userRepository.findByEmail(normalizeEmail(request.getEmail()))
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_EXIST));
        verifyOtp(user, "forgot", request.getOtp());
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        redisTemplate.delete(otpKey(user.getId(), "forgot"));
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.PASSWORD_CONFIRM_MISMATCH);
        }
        User user = currentUserService.getCurrentUser();
        if (user.getPassword() == null || !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private void sendOtp(User user, String purpose, String subject) {
        enforceRateLimit(user.getId(), purpose);
        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        redisTemplate.opsForValue().set(otpKey(user.getId(), purpose), passwordEncoder.encode(otp), OTP_TTL);
        redisTemplate.opsForValue().set(cooldownKey(user.getId(), purpose), "1", RESEND_COOLDOWN);

        mailService.sendSimpleMail(
                user.getEmail(),
                subject,
                """
                Ma OTP cua ban la: %s

                Ma co hieu luc trong 5 phut. Khong chia se ma nay cho nguoi khac.
                """.formatted(otp)
        );
    }

    private void verifyOtp(User user, String purpose, String otp) {
        Object hash = redisTemplate.opsForValue().get(otpKey(user.getId(), purpose));
        if (hash == null || !passwordEncoder.matches(otp, hash.toString())) {
            throw new AppException(ErrorCode.OTP_INVALID);
        }
    }

    private void enforceRateLimit(String userId, String purpose) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey(userId, purpose)))) {
            throw new AppException(ErrorCode.OTP_RATE_LIMITED);
        }
        Long count = redisTemplate.opsForValue().increment(rateKey(userId, purpose));
        if (count != null && count == 1) {
            redisTemplate.expire(rateKey(userId, purpose), RATE_WINDOW);
        }
        if (count != null && count > MAX_REQUESTS) {
            throw new AppException(ErrorCode.OTP_RATE_LIMITED);
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private String otpKey(String userId, String purpose) {
        return "otp:password:" + purpose + ":" + userId;
    }

    private String cooldownKey(String userId, String purpose) {
        return "otp:password:" + purpose + ":cooldown:" + userId;
    }

    private String rateKey(String userId, String purpose) {
        return "otp:password:" + purpose + ":rate:" + userId;
    }
}
