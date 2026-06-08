package org.example.stationery_shop.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.request.PhoneOtpSendRequest;
import org.example.stationery_shop.dto.request.PhoneOtpVerifyRequest;
import org.example.stationery_shop.entity.auth.User;
import org.example.stationery_shop.exception.AppException;
import org.example.stationery_shop.exception.ErrorCode;
import org.example.stationery_shop.repository.UserRepository;
import org.example.stationery_shop.service.CurrentUserService;
import org.example.stationery_shop.service.PhoneVerificationService;
import org.example.stationery_shop.service.auth.MailService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class PhoneVerificationServiceImpl implements PhoneVerificationService {
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
    public void sendOtp(PhoneOtpSendRequest request) {
        User user = currentUserService.getCurrentUser();
        String phone = normalizePhone(request.getPhone());
        ensurePhoneAvailable(user, phone);
        enforceRateLimit(user.getId());

        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        redisTemplate.opsForValue().set(otpKey(user.getId(), phone), passwordEncoder.encode(otp), OTP_TTL);
        redisTemplate.opsForValue().set(cooldownKey(user.getId()), "1", RESEND_COOLDOWN);

        mailService.sendSimpleMail(
                user.getEmail(),
                "Ma OTP xac thuc so dien thoai",
                """
                Ma OTP xac thuc so dien thoai %s la: %s

                Ma co hieu luc trong 5 phut. Khong chia se ma nay cho nguoi khac.
                """.formatted(phone, otp)
        );
    }

    @Override
    @Transactional
    public void verifyOtp(PhoneOtpVerifyRequest request) {
        User user = currentUserService.getCurrentUser();
        String phone = normalizePhone(request.getPhone());
        ensurePhoneAvailable(user, phone);

        Object hash = redisTemplate.opsForValue().get(otpKey(user.getId(), phone));
        if (hash == null || !passwordEncoder.matches(request.getOtp(), hash.toString())) {
            throw new AppException(ErrorCode.OTP_INVALID);
        }

        user.setPhone(phone);
        user.setPhoneVerified(true);
        userRepository.save(user);
        redisTemplate.delete(otpKey(user.getId(), phone));
    }

    private void enforceRateLimit(String userId) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey(userId)))) {
            throw new AppException(ErrorCode.OTP_RATE_LIMITED);
        }
        Long count = redisTemplate.opsForValue().increment(rateKey(userId));
        if (count != null && count == 1) {
            redisTemplate.expire(rateKey(userId), RATE_WINDOW);
        }
        if (count != null && count > MAX_REQUESTS) {
            throw new AppException(ErrorCode.OTP_RATE_LIMITED);
        }
    }

    private void ensurePhoneAvailable(User user, String phone) {
        if (userRepository.existsByPhoneAndIdNot(phone, user.getId())) {
            throw new AppException(ErrorCode.PHONE_EXISTED);
        }
    }

    private String normalizePhone(String phone) {
        return phone == null ? null : phone.trim();
    }

    private String otpKey(String userId, String phone) {
        return "otp:phone:verify:" + userId + ":" + phone;
    }

    private String cooldownKey(String userId) {
        return "otp:phone:cooldown:" + userId;
    }

    private String rateKey(String userId) {
        return "otp:phone:rate:" + userId;
    }
}
