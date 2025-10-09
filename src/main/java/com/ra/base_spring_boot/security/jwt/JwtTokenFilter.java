package com.ra.base_spring_boot.security.jwt;

import com.ra.base_spring_boot.security.principle.MyUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
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

        log.info("🔍 [JwtTokenFilter] Filtering request: {}", request.getRequestURI());

        try {
            String token = getTokenFromRequest(request);
            log.info("📦 [JwtTokenFilter] Token from request: {}", token);

            if (token != null) {
                String username = jwtProvider.extractUsername(token);
                log.info("👤 [JwtTokenFilter] Username extracted from token: {}", username);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                log.info("✅ [JwtTokenFilter] Loaded user details: {}", userDetails.getUsername());

                boolean isValid = jwtProvider.validateToken(token, userDetails);
                log.info("🧩 [JwtTokenFilter] Token valid: {}", isValid);

                if (isValid) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("🔐 [JwtTokenFilter] Authentication set for user: {}", username);
                } else {
                    log.warn("🚫 [JwtTokenFilter] Token validation failed for user: {}", username);
                }
            } else {
                log.warn("⚠️ [JwtTokenFilter] No JWT token found in request headers");
            }
        } catch (ExpiredJwtException e) {
            log.error("⏰ [JwtTokenFilter] JWT expired: {}", e.getMessage());
        } catch (MalformedJwtException | SignatureException e) {
            log.error("❌ [JwtTokenFilter] Invalid JWT: {}", e.getMessage());
        } catch (Exception e) {
            log.error("💥 [JwtTokenFilter] Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
