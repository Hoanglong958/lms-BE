package com.ra.base_spring_boot.config.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ra.base_spring_boot.dto.req.ForgotPasswordRequest;
import com.ra.base_spring_boot.dto.req.ResetPasswordRequest;
import com.ra.base_spring_boot.dto.resp.PasswordResetTokenResponse;
import com.ra.base_spring_boot.model.PasswordResetToken;
import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.model.constants.RoleName;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.repository.IPasswordResetTokenRepository;
import com.ra.base_spring_boot.repository.IUserRepository;
import com.ra.base_spring_boot.services.IPasswordResetTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test cho toàn bộ flow quên mật khẩu (Delayed OTP Reveal Flow)
 * 
 * Flow:
 * 1. User gọi forgot-password → Server tạo token và gửi link
 * 2. Dev lấy token mới nhất bằng /latest endpoint
 * 3. Validate token
 * 4. Reset password với token
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // Tắt security filter cho test
@DisplayName("Forgot Password Flow Integration Test")
class ForgotPasswordFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IPasswordResetTokenService passwordResetTokenService;

    @MockBean
    private IPasswordResetTokenRepository tokenRepository;

    @MockBean
    private IUserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private User testUser;
    private PasswordResetToken testToken;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = User.builder()
                .id(1L)
                .email("testuser@gmail.com")
                .fullName("Test User")
                .password("$2a$10$encrypted")
                .role(RoleName.ROLE_USER)
                .isActive(true)
                .build();

        // Setup test token
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
    @DisplayName("Test toàn bộ flow quên mật khẩu: forgot → get latest → validate → reset")
    void testCompleteForgotPasswordFlow() throws Exception {
        // ========== BƯỚC 1: Gọi forgot-password ==========
        ForgotPasswordRequest forgotRequest = new ForgotPasswordRequest();
        forgotRequest.setEmail("testuser@gmail.com");

        PasswordResetTokenResponse tokenResponse = PasswordResetTokenResponse.builder()
                .id(1L)
                .userId(1L)
                .token("test-token-12345")
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .isUsed(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByEmail("testuser@gmail.com"))
                .thenReturn(Optional.of(testUser));
        when(passwordResetTokenService.create(any()))
                .thenReturn(tokenResponse);

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(forgotRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.containsString("Đã gửi link")));

        verify(passwordResetTokenService).create(any());

        // ========== BƯỚC 2: Lấy token mới nhất (DEV endpoint) ==========
        when(tokenRepository.findTopByOrderByCreatedAtDesc())
                .thenReturn(Optional.of(testToken));

        mockMvc.perform(get("/api/v1/password-reset-tokens/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("test-token-12345"))
                .andExpect(jsonPath("$.data.userId").value(1L))
                .andExpect(jsonPath("$.data.isUsed").value(false));

        verify(tokenRepository).findTopByOrderByCreatedAtDesc();

        // ========== BƯỚC 3: Validate token ==========
        when(passwordResetTokenService.validateToken("test-token-12345"))
                .thenReturn(true);

        mockMvc.perform(get("/api/v1/password-reset-tokens/validate")
                        .param("token", "test-token-12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        verify(passwordResetTokenService).validateToken("test-token-12345");

        // ========== BƯỚC 4: Reset password ==========
        ResetPasswordRequest resetRequest = new ResetPasswordRequest();
        resetRequest.setToken("test-token-12345");
        resetRequest.setNewPassword("NewPassword123!");

        doNothing().when(passwordResetTokenService).resetPassword(any(ResetPasswordRequest.class));

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.containsString("Đặt lại mật khẩu thành công")));

        verify(passwordResetTokenService).resetPassword(any(ResetPasswordRequest.class));
    }

    @Test
    @DisplayName("Test flow với token không hợp lệ")
    void testFlowWithInvalidToken() throws Exception {
        // Bước 1: Forgot password
        ForgotPasswordRequest forgotRequest = new ForgotPasswordRequest();
        forgotRequest.setEmail("testuser@gmail.com");

        when(userRepository.findByEmail("testuser@gmail.com"))
                .thenReturn(Optional.of(testUser));
        when(passwordResetTokenService.create(any()))
                .thenReturn(PasswordResetTokenResponse.builder()
                        .token("valid-token")
                        .build());

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(forgotRequest)))
                .andExpect(status().isOk());

        // Bước 2: Validate token không hợp lệ
        when(passwordResetTokenService.validateToken("invalid-token"))
                .thenReturn(false);

        mockMvc.perform(get("/api/v1/password-reset-tokens/validate")
                        .param("token", "invalid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(false));

        // Bước 3: Reset password với token không hợp lệ → Fail
        ResetPasswordRequest resetRequest = new ResetPasswordRequest();
        resetRequest.setToken("invalid-token");
        resetRequest.setNewPassword("NewPassword123!");

        doThrow(new HttpBadRequest("Token không hợp lệ!"))
                .when(passwordResetTokenService).resetPassword(any(ResetPasswordRequest.class));

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetRequest)))
                .andExpect(status().isBadRequest());
    }
}

