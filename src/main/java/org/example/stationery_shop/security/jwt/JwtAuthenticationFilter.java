package org.example.stationery_shop.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.stationery_shop.dto.response.ApiResponse;
import org.example.stationery_shop.exception.AppException;
import org.example.stationery_shop.exception.ErrorCode;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestPath = request.getServletPath();
        String jwtToken = getTokenFromRequest(request, requestPath);

        if (jwtToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            var claims = jwtService.parseToken(jwtToken).getBody();
            String tokenType = claims.get("tokenType", String.class);

            if ("/api/auth/refreshToken".equals(requestPath)
                    && "POST".equalsIgnoreCase(request.getMethod())) {
                if (!"REFRESH".equals(tokenType)) {
                    throw new AppException(ErrorCode.TOKEN_MUST_BE_REFRESH);
                }
                filterChain.doFilter(request, response);
                return;
            }

            if (("/api/auth/logout".equals(requestPath) || "/api/auth/logout-all".equals(requestPath))
                    && "POST".equalsIgnoreCase(request.getMethod())) {
                if (!"REFRESH".equals(tokenType)) {
                    throw new AppException(ErrorCode.TOKEN_MUST_BE_REFRESH);
                }
                filterChain.doFilter(request, response);
                return;
            }

            if (!"ACCESS".equals(tokenType)) {
                throw new AppException(ErrorCode.TOKEN_MUST_BE_ACCESS);
            }

            List<String> roles = claims.get("roles", List.class);
            List<String> permissions = claims.get("permissions", List.class);

            if (roles == null) {
                roles = new ArrayList<>();
            }
            if (permissions == null) {
                permissions = new ArrayList<>();
            }

            List<GrantedAuthority> authorities = new ArrayList<>();
            roles.forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));
            permissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));
            for (GrantedAuthority authority : authorities) {
                log.warn("authority: {}", authority.getAuthority().toString());
            }
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            claims.getSubject(),
                            null,
                            authorities
                    );

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authToken);
            log.warn("path: {}, method: {}", request.getServletPath(), request.getMethod());
            log.warn("token exists: {}", jwtToken != null);
            log.warn("tokenType: {}", tokenType);
            log.warn("security auth after set: {}", SecurityContextHolder.getContext().getAuthentication());

            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            handleJwtError(response, ErrorCode.TOKEN_EXPIRED);
        } catch (SignatureException | MalformedJwtException |
                 UnsupportedJwtException | IllegalArgumentException e) {
            handleJwtError(response, ErrorCode.NOT_VALID_TOKEN);
        } catch (AppException e) {
            handleJwtError(response, e.getErrorCode());
        }
    }

    private void handleJwtError(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatusCode().value());
        response.setContentType("application/json;charset=UTF-8");

        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }

    private String getTokenFromRequest(HttpServletRequest request, String requestPath) {
        boolean needRefreshToken =
                ("/api/auth/refreshToken".equals(requestPath) && "POST".equalsIgnoreCase(request.getMethod())) ||
                        ("/api/auth/logout".equals(requestPath) && "POST".equalsIgnoreCase(request.getMethod())) ||
                        ("/api/auth/logout-all".equals(requestPath) && "POST".equalsIgnoreCase(request.getMethod()));

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            String cookieName = needRefreshToken ? "refreshToken" : "accessToken";
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }

        return null;
    }
}
