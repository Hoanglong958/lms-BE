package com.ra.base_spring_boot.security;

import com.ra.base_spring_boot.model.constants.RoleName;
import com.ra.base_spring_boot.security.exception.AccessDenied;
import com.ra.base_spring_boot.security.exception.JwtEntryPoint;
import com.ra.base_spring_boot.security.jwt.JwtTokenFilter;
import com.ra.base_spring_boot.security.principle.MyUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

        private final MyUserDetailsService userDetailsService;
        private final JwtEntryPoint jwtEntryPoint;
        private final AccessDenied accessDenied;
        private final JwtTokenFilter jwtTokenFilter;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                return http
                                .cors(cors -> cors.configurationSource(request -> {
                                        CorsConfiguration config = new CorsConfiguration();
                                        config.setAllowedOrigins(List.of("http://localhost:5173"));
                                        config.addAllowedOriginPattern("*");
                                        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                                        config.setAllowedHeaders(List.of("*"));
                                        config.setAllowCredentials(true);
                                        config.setExposedHeaders(List.of("Authorization"));
                                        return config;
                                }))
                                .csrf(AbstractHttpConfigurer::disable)
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                                // Chỉ cho phép các endpoint public thuộc auth
                                                // (login/register/forgot/reset/verify)
                                                .requestMatchers(
                                                                "/api/v1/auth/login",
                                                                "/api/v1/auth/register",
                                                                "/api/v1/auth/forgot-password-otp",
                                                                "/api/v1/auth/verify-otp",
                                                                "/api/v1/auth/reset-password")
                                                .permitAll()
                                                .requestMatchers(

                                                                "/api/v1/users/check",
                                                                // Public static uploads (served by WebMvc)
                                                                "/uploads/**",
                                                                "/api/v1/uploads/**",
                                                                // Public password reset token endpoints (delayed OTP
                                                                // reveal flow)
                                                                "/api/v1/password-reset-tokens/validate",
                                                                "/api/v1/password-reset-tokens/latest", // DEV endpoint:
                                                                                                        // Lấy token mới
                                                                                                        // nhất để test
                                                                // Swagger and docs
                                                                "/v3/api-docs/**",
                                                                "/docs",
                                                                "/docs/**",
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html",
                                                                "/docs/**",
                                                                // WebSocket endpoints (if any)
                                                                "/ws/**",
                                                                "/ws-raw/**",
                                                                // Error handling endpoint
                                                                "/error",
                                                                "/error/**")
                                                .permitAll()
                                                .requestMatchers("/api/v1/admin/**")
                                                .hasAuthority(RoleName.ROLE_ADMIN.name())
                                                .requestMatchers("/api/v1/questions/**")
                                                .hasAnyAuthority(RoleName.ROLE_ADMIN.name(), RoleName.ROLE_USER.name())
                                                // Các endpoint quản lý user: chỉ ADMIN
                                                .requestMatchers("/api/v1/posts/**")
                                                .hasAnyAuthority(RoleName.ROLE_ADMIN.name(), RoleName.ROLE_USER.name(),
                                                                RoleName.ROLE_TEACHER.name())
                                                .requestMatchers("/api/v1/users/**").hasAnyAuthority(
                                                                RoleName.ROLE_ADMIN.name(),
                                                                RoleName.ROLE_TEACHER.name(),
                                                                RoleName.ROLE_USER.name())
                                                .anyRequest().authenticated())
                                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint(jwtEntryPoint)
                                                .accessDeniedHandler(accessDenied))
                                .authenticationProvider(authenticationProvider())
                                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                                .build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        @SuppressWarnings("deprecation")
        public AuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
                provider.setUserDetailsService(userDetailsService);
                provider.setPasswordEncoder(passwordEncoder());
                return provider;
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration auth) throws Exception {
                return auth.getAuthenticationManager();
        }

}
