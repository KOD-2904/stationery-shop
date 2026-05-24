package org.example.stationery_shop.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.stationery_shop.entity.auth.Permission;
import org.example.stationery_shop.entity.auth.Role;
import org.example.stationery_shop.entity.auth.User;
import org.example.stationery_shop.exception.AppException;
import org.example.stationery_shop.exception.ErrorCode;
import org.example.stationery_shop.security.user.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class JwtService {
    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    public Jws<Claims> parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
    }
    public CustomUserDetails getUserDetails(Authentication authentication) {
        return (CustomUserDetails) authentication.getPrincipal();
    }
    public JwtTokenResult generateRefreshTokenResultFromAuthentication(Authentication authentication) {
        var user = getUserDetails(authentication).getUser();
        if (user == null) {
            throw new AppException(ErrorCode.USER_DETAILS_IS_NULL);
        }
        return generateRefreshTokenResult(user);
    }
    public JwtTokenResult generateRefreshTokenResult(User user) {
        Claims claims = buildClaims(
                user,
                "REFRESH",
                jwtProperties.getRefreshTokenExpiration()
        );
        String token = Jwts.builder()
                .setClaims(claims)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();

        return JwtTokenResult.builder()
                .token(token)
                .jti(claims.getId())
                .userId(user.getId())
                .issuedAt(claims.getIssuedAt())
                .expiredAt(claims.getExpiration())
                .build();
    }
    public String generateAccessTokenFromAuthentication(Authentication authentication) {
        var user = getUserDetails(authentication).getUser();
        if (user == null) {
            throw new AppException(ErrorCode.USER_DETAILS_IS_NULL);
        }
        return generateAccessToken(user);
    }
    public String generateAccessToken(User user) {

        Claims claims = buildClaims(
                user,
                "ACCESS",
                jwtProperties.getAccessTokenExpiration()
        );

        return Jwts.builder()
                .setClaims(claims)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }
    private Claims buildClaims(User user, String tokenType, long expiration) {
        //co 2 cach de tiep can viec lay role va permission
//        List<String> roles = userDetails.getAuthorities().stream()
//                .map(GrantedAuthority::getAuthority).filter(Objects::nonNull)
//                .filter(auth -> auth.startsWith("ROLE_"))
//                .toList();
//
//        List<String> permissions = userDetails.getAuthorities().stream()
//                .map(GrantedAuthority::getAuthority).filter(Objects::nonNull)
//                .filter(auth -> !auth.startsWith("ROLE_"))
//                .toList();

        //cach nay tranh phu thuoc vao naming convention (ROLE_)
        //lỡ đổi prefix van de thay doi
        List<String> roles = user.getRoles().stream()
                .map(Role::getCode)
                .toList();

        List<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getCode)
                .toList();
        Claims claims = Jwts.claims()
                .setId(UUID.randomUUID().toString())
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration));

        if(tokenType.equals("ACCESS")) {
            claims.put("roles", roles);
            claims.put("permissions", permissions);
        }
        claims.put("tokenType", tokenType);
        claims.put("userId", user.getId());
        return claims;
    }
}
