package com.ra.base_spring_boot.security.jwt;

import com.ra.base_spring_boot.security.principle.MyUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final MyUserDetailsService userDetailsService;
    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        
        // ✅ Bỏ qua preflight CORS (OPTIONS) để tránh xử lý JWT không cần thiết
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            log.debug("[JwtTokenFilter] Skipping JWT for OPTIONS request: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ Bỏ qua các endpoint public (không yêu cầu JWT)
        // Liệt kê các endpoint public trong isPublicEndpoint(...) - không skip tất cả /api/v1/auth/*
        if (isPublicEndpoint(path)) {
            log.info("[JwtTokenFilter] Skipping JWT validation for public endpoint: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        log.info("🔍 [JwtTokenFilter] Checking JWT for request: {}", path);

        try {
            String token = getTokenFromRequest(request);
            if (token != null) {
                String username = jwtProvider.extractUsername(token);
                log.info("👤 Username extracted from token: {}", username);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                boolean isValid = jwtProvider.validateToken(token, userDetails);

                if (isValid) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("✅ Authentication set for user: {}", username);
                } else {
                    log.warn("🚫 Token validation failed for user: {}", username);
                }
            } else {
                log.warn("⚠️ No JWT token found in request headers");
            }
        } catch (ExpiredJwtException e) {
            log.error("⏰ JWT expired: {}", e.getMessage());
        } catch (MalformedJwtException | SignatureException e) {
            log.error("❌ Invalid JWT: {}", e.getMessage());
        } catch (Exception e) {
            log.error("💥 Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * ✅ Các endpoint public được phép truy cập mà không cần token
     */
    private boolean isPublicEndpoint(String path) {
        return path.equals("/api/v1/auth/login")
                || path.equals("/api/v1/auth/register")
                || path.equals("/api/v1/auth/forgot-password")
                || path.startsWith("/api/v1/auth/forgot-password/")
                || path.equals("/api/v1/auth/reset-password")
                || path.startsWith("/api/v1/auth/reset-password/")
                // Public password reset token endpoints (delayed OTP reveal flow)
                || path.equals("/api/v1/password-reset-tokens/validate")
                || path.equals("/api/v1/password-reset-tokens/latest") // DEV endpoint: Lấy token mới nhất để test
                // Public user endpoints
                || path.equals("/api/v1/users/check")                // Public uploads
                || path.equals("/api/v1/uploads")
                || path.startsWith("/api/v1/uploads/")                || path.equals("/v3/api-docs")
                || path.startsWith("/v3/api-docs/")
                || path.startsWith("/swagger-ui")
                || path.equals("/swagger-ui.html")
                || path.startsWith("/docs/")
                || path.startsWith("/ws/")
                || path.equals("/error")
                || path.startsWith("/error/");
    }

    /**
     * ✅ Lấy JWT token từ header Authorization
     * Dạng hợp lệ: Bearer <jwt_token>
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
