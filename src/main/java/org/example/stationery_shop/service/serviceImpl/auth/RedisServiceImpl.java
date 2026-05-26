package org.example.stationery_shop.service.serviceImpl.auth;

import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.request.RefreshTokenSession;
import org.example.stationery_shop.security.jwt.JwtProperties;
import org.example.stationery_shop.service.auth.RedisService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtProperties jwtProperties;

    private static final String REFRESH_TOKEN_KEY = "auth:refresh_token:";
    private static final String USER_SESSIONS_KEY = "auth:user_sessions:";
    @Override
    public void saveRefreshTokenSession(
            String userId,
            String jti,
            String device,
            String ip,
            String userAgent
    ) {
        RefreshTokenSession session = RefreshTokenSession.builder()
                .jti(jti)
                .userId(userId)
                .device(device)
                .ip(ip)
                .userAgent(userAgent)
                .loginAt(Instant.now())
                .build();

        String tokenKey = REFRESH_TOKEN_KEY + jti;
        String userSessionsKey = USER_SESSIONS_KEY + userId;

        Duration ttl = jwtProperties.getRefreshTokenDuration();

        redisTemplate.opsForValue().set(tokenKey, session, ttl);

        redisTemplate.opsForSet().add(userSessionsKey, jti);

        // Chỉ set expire nếu key chưa tồn tại
        if (Boolean.TRUE.equals(redisTemplate.hasKey(userSessionsKey))) {
            Long currentTTL = redisTemplate.getExpire(userSessionsKey);
            if (currentTTL == null || currentTTL < 0) {
                redisTemplate.expire(userSessionsKey, ttl);
            }
        } else {
            redisTemplate.expire(userSessionsKey, ttl);
        }
    }
    @Override
    public RefreshTokenSession getSession(String jti) {
        Object value = redisTemplate.opsForValue()
                .get(REFRESH_TOKEN_KEY + jti);

        if (value == null) {
            return null;
        }

        return (RefreshTokenSession) value;
    }
    @Override
    public boolean isRefreshTokenValid(String jti) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(REFRESH_TOKEN_KEY + jti)
        );
    }
    @Override
    public void logoutOneDevice(String userId, String jti) {
        revokeRefreshToken(userId, jti);
    }
    public void revokeRefreshToken(String userId, String jti) {
        redisTemplate.delete(REFRESH_TOKEN_KEY + jti);
        redisTemplate.opsForSet()
                .remove(USER_SESSIONS_KEY + userId, jti);
    }
    @Override
    public void logoutAllDevices(String userId) {
        String userSessionsKey = USER_SESSIONS_KEY + userId;

        var jtis = redisTemplate.opsForSet()
                .members(userSessionsKey);

        if (jtis != null) {
            for (Object jti : jtis) {
                redisTemplate.delete(REFRESH_TOKEN_KEY + jti);
            }
        }

        redisTemplate.delete(userSessionsKey);
    }

    @Override
    public void getAllSessions(String userId) {

    }
}
