package org.example.stationery_shop.security.jwt;

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
import org.example.stationery_shop.security.user.UserDetailServiceImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    //private final UserDetailServiceImpl userDetailService;
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestPath = request.getServletPath();
        String jwtToken = getTokenFromRequest(request,requestPath);

        // 1. Không có token → đi tiếp luôn
        if (jwtToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            var claims = jwtService.parseToken(jwtToken).getBody();
            String tokenType = claims.get("tokenType", String.class);

            // 2. Refresh endpoint → cần REFRESH token
            if ("/api/auth/refresh".equals(requestPath)
                    && "POST".equalsIgnoreCase(request.getMethod())) {

                if (!"REFRESH".equals(tokenType)) {
                    throw new AppException(ErrorCode.TOKEN_MUST_BE_REFRESH);
                }
                // Không set authentication cho refresh
                filterChain.doFilter(request, response);
                return;
            }

            // 3. Logout endpoint → cần REFRESH token
            if ("/api/auth/logout".equals(requestPath)
                    && "POST".equalsIgnoreCase(request.getMethod())) {

                if (!"REFRESH".equals(tokenType)) {
                    throw new AppException(ErrorCode.TOKEN_MUST_BE_REFRESH);
                }
                // Không set authentication cho logout
                filterChain.doFilter(request, response);
                return;
            }

            // 4. Các endpoint khác → cần ACCESS token
            if (!"ACCESS".equals(tokenType)) {
                throw new AppException(ErrorCode.TOKEN_MUST_BE_ACCESS);
            }
            // đoạn này có 2 hướng: 1. statless _ dùng jwt full, tức là hiện tại đang làm là
//                //nhét role và permission vào token luôn, tất hiên sẽ cso trade - off là:
//                //User bị revoke quyền → token cũ vẫn còn hiệu lực
//                //Permission thay đổi → phải chờ token expire
//                //2. không bỏ role và permission vào token, nen mõi request sẽ load user từ DB để láy authority
//                // ưu điểm là authority có hiệu lực ngay lập tức
//                //nhưng đánh đổi là mõi request lại một lần gọi DB không stateless
//
//                // Set authentication cho access token
//                String username = claims.getSubject();
//                UserDetails userDetails = userDetailService.loadUserByUsername(username);
//                UsernamePasswordAuthenticationToken authToken =
//                        new UsernamePasswordAuthenticationToken(
//                                userDetails,
//                                null,
//                                userDetails.getAuthorities()
//                        );
//                //SecurityContextHolder.getContext().setAuthentication(authToken);
//                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                if (SecurityContextHolder.getContext().getAuthentication() == null) {
//                    SecurityContextHolder.getContext().setAuthentication(authToken);
//                }

            // 4. Build Authentication
            List<String> roles = claims.get("roles", List.class);
            List<String> permissions = claims.get("permissions", List.class);

            if (roles == null) roles = new ArrayList<>();
            if (permissions == null) permissions = new ArrayList<>();

            List<GrantedAuthority> authorities = new ArrayList<>();

            roles.forEach(r -> authorities.add(new SimpleGrantedAuthority(r)));
            permissions.forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            claims.getSubject(),
                            null,
                            authorities
                    );

            authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

            filterChain.doFilter(request, response);
            return;

        } catch (ExpiredJwtException e) {
            handleJwtError(response, ErrorCode.TOKEN_EXPIRED);
            return;

        } catch (SignatureException | MalformedJwtException |
                 UnsupportedJwtException | IllegalArgumentException e) {
            handleJwtError(response, ErrorCode.NOT_VALID_TOKEN);
        } catch (AppException e) {
            handleJwtError(response, e.getErrorCode());
        }
    }
    private void handleJwtError(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatusCode().value());
        response.setContentType("application/json");

        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }

    private String getTokenFromRequest(HttpServletRequest request, String requestPath) {

        // Danh sách endpoint cần REFRESH token
        boolean needRefreshToken =
                ("/api/auth/refreshToken".equals(requestPath) && "POST".equalsIgnoreCase(request.getMethod())) ||
                        ("/api/auth/logout".equals(requestPath) && "POST".equalsIgnoreCase(request.getMethod()));

        // 1. Thử lấy từ Header trước (cho mobile app)
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }

        // 2. Lấy từ Cookie (cho web browser)
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            if (needRefreshToken) {
                // Endpoint cần refresh token
                for (Cookie cookie : cookies) {
                    if ("refreshToken".equals(cookie.getName())) {
                        return cookie.getValue();
                    }
                }
            } else {
                // Các endpoint khác cần access token
                for (Cookie cookie : cookies) {
                    if ("accessToken".equals(cookie.getName())) {
                        return cookie.getValue();
                    }
                }
            }
        }

        return null;
    }
}
