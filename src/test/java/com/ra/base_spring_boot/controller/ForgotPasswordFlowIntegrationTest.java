package com.ra.base_spring_boot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ra.base_spring_boot.dto.req.ForgotPasswordRequest;
import com.ra.base_spring_boot.dto.req.ResetPasswordRequest;
import com.ra.base_spring_boot.dto.resp.PasswordResetTokenResponse;
import com.ra.base_spring_boot.model.PasswordResetToken;
import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.model.constants.RoleName;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.repository.auth.IPasswordResetTokenRepository;
import com.ra.base_spring_boot.repository.user.IUserRepository;
import com.ra.base_spring_boot.services.auth.IPasswordResetTokenService;
import com.ra.base_spring_boot.services.auth.IAuthService;
import com.ra.base_spring_boot.security.principle.MyUserDetailsService;
import com.ra.base_spring_boot.security.jwt.JwtProvider;
import com.ra.base_spring_boot.security.jwt.JwtTokenFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = { AuthController.class, PasswordResetTokenController.class }, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Forgot Password Flow Integration Test")
@SuppressWarnings("removal")
class ForgotPasswordFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IPasswordResetTokenService passwordResetTokenService;

    @Autowired
    private IPasswordResetTokenRepository tokenRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IAuthService authService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private User testUser;
    private PasswordResetToken testToken;

    @SuppressWarnings("unused")
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .gmail("testuser@gmail.com")
                .fullName("Test User")
                .password("$2a$10$encrypted")
                .role(RoleName.ROLE_USER)
                .isActive(true)
                .build();

        testToken = PasswordResetToken.builder()
                .id(1L)
                .user(testUser)
                .token("test-token-12345")
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .isUsed(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("testCompleteForgotPasswordFlow")
    void testCompleteForgotPasswordFlow() throws Exception {
        ForgotPasswordRequest forgotRequest = new ForgotPasswordRequest();
        forgotRequest.setGmail("testuser@gmail.com");

        PasswordResetTokenResponse tokenResponse = PasswordResetTokenResponse.builder()
                .id(1L)
                .userId(1L)
                .token("test-token-12345")
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .isUsed(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByGmail("testuser@gmail.com"))
                .thenReturn(Optional.of(testUser));
        when(passwordResetTokenService.create(any()))
                .thenReturn(tokenResponse);

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(forgotRequest))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(passwordResetTokenService).create(any());

        when(tokenRepository.findTopByOrderByCreatedAtDesc())
                .thenReturn(Optional.of(testToken));

        mockMvc.perform(get("/api/v1/password-reset-tokens/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("test-token-12345"));

        verify(tokenRepository).findTopByOrderByCreatedAtDesc();

        when(passwordResetTokenService.validateToken("test-token-12345"))
                .thenReturn(true);

        mockMvc.perform(get("/api/v1/password-reset-tokens/validate")
                .param("token", "test-token-12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        verify(passwordResetTokenService).validateToken("test-token-12345");

        ResetPasswordRequest resetRequest = new ResetPasswordRequest();
        resetRequest.setToken("test-token-12345");
        resetRequest.setNewPassword("NewPassword123!");

        doNothing().when(authService).resetPassword(any(ResetPasswordRequest.class));

        mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(resetRequest))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(authService).resetPassword(any(ResetPasswordRequest.class));
    }

    @Test
    @DisplayName("testFlowWithInvalidToken")
    void testFlowWithInvalidToken() throws Exception {
        ForgotPasswordRequest forgotRequest = new ForgotPasswordRequest();
        forgotRequest.setGmail("testuser@gmail.com");

        when(userRepository.findByGmail("testuser@gmail.com"))
                .thenReturn(Optional.of(testUser));
        when(passwordResetTokenService.create(any()))
                .thenReturn(PasswordResetTokenResponse.builder()
                        .token("valid-token")
                        .build());

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(forgotRequest))))
                .andExpect(status().isOk());

        when(passwordResetTokenService.validateToken("invalid-token"))
                .thenReturn(false);

        mockMvc.perform(get("/api/v1/password-reset-tokens/validate")
                .param("token", "invalid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(false));

        ResetPasswordRequest resetRequest = new ResetPasswordRequest();
        resetRequest.setToken("invalid-token");
        resetRequest.setNewPassword("NewPassword123!");

        doThrow(new HttpBadRequest("Token không hợp lệ!"))
                .when(authService).resetPassword(any(ResetPasswordRequest.class));

        mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(resetRequest))))
                .andExpect(status().isBadRequest());
    }

    @TestConfiguration
    static class MockConfig {
        @Bean
        IPasswordResetTokenService passwordResetTokenService() {
            return org.mockito.Mockito.mock(IPasswordResetTokenService.class);
        }

        @Bean
        IPasswordResetTokenRepository tokenRepository() {
            return org.mockito.Mockito.mock(IPasswordResetTokenRepository.class);
        }

        @Bean
        IUserRepository userRepository() {
            return org.mockito.Mockito.mock(IUserRepository.class);
        }

        @Bean
        IAuthService authService() {
            return org.mockito.Mockito.mock(IAuthService.class);
        }

        @Bean
        MyUserDetailsService myUserDetailsService() {
            return org.mockito.Mockito.mock(MyUserDetailsService.class);
        }

        @Bean
        JwtProvider jwtProvider() {
            return org.mockito.Mockito.mock(JwtProvider.class);
        }

        @Bean
        JwtTokenFilter jwtTokenFilter() {
            return org.mockito.Mockito.mock(JwtTokenFilter.class);
        }
    }
}
