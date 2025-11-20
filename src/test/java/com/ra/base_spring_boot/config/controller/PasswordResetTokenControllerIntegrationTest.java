package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.dto.resp.PasswordResetTokenResponse;
import com.ra.base_spring_boot.model.PasswordResetToken;
import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.repository.IPasswordResetTokenRepository;
import com.ra.base_spring_boot.services.IPasswordResetTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = PasswordResetTokenController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class PasswordResetTokenControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IPasswordResetTokenService tokenService;

    @MockBean
    private IPasswordResetTokenRepository tokenRepository;

    // ==================== TEST VALIDATE TOKEN (Delayed OTP Reveal Flow) ====================

    @Test
    void validateToken_shouldReturn200WithTrueWhenTokenValid() throws Exception {
        // Given: Token hợp lệ, chưa dùng, chưa hết hạn
        when(tokenService.validateToken("valid-token")).thenReturn(true);

        // When & Then: API trả về true
        mockMvc.perform(get("/api/v1/password-reset-tokens/validate")
                        .param("token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        verify(tokenService).validateToken("valid-token");
    }

    @Test
    void validateToken_shouldReturn200WithFalseWhenTokenInvalid() throws Exception {
        // Given: Token không tồn tại hoặc không hợp lệ
        when(tokenService.validateToken("invalid-token")).thenReturn(false);

        // When & Then: API trả về false
        mockMvc.perform(get("/api/v1/password-reset-tokens/validate")
                        .param("token", "invalid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    void validateToken_shouldReturn200WithFalseWhenTokenExpired() throws Exception {
        // Given: Token đã hết hạn
        when(tokenService.validateToken("expired-token")).thenReturn(false);

        // When & Then: API trả về false
        mockMvc.perform(get("/api/v1/password-reset-tokens/validate")
                        .param("token", "expired-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    void validateToken_shouldReturn200WithFalseWhenTokenAlreadyUsed() throws Exception {
        // Given: Token đã được sử dụng
        when(tokenService.validateToken("used-token")).thenReturn(false);

        // When & Then: API trả về false
        mockMvc.perform(get("/api/v1/password-reset-tokens/validate")
                        .param("token", "used-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(false));
    }

    // ==================== TEST DEV ENDPOINT: GET LATEST TOKEN ====================

    @Test
    void getLatestToken_shouldReturn200WithTokenWhenExists() throws Exception {
        // Given: Có token mới nhất trong DB
        User mockUser = User.builder()
                .id(100L)
                .email("test@gmail.com")
                .build();

        PasswordResetToken latestToken = PasswordResetToken.builder()
                .id(1L)
                .user(mockUser)
                .token("latest-token-123")
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .isUsed(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(tokenRepository.findTopByOrderByCreatedAtDesc())
                .thenReturn(java.util.Optional.of(latestToken));

        // When & Then: API trả về token mới nhất
        mockMvc.perform(get("/api/v1/password-reset-tokens/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.userId").value(100L))
                .andExpect(jsonPath("$.data.token").value("latest-token-123"))
                .andExpect(jsonPath("$.data.isUsed").value(false));

        verify(tokenRepository).findTopByOrderByCreatedAtDesc();
    }

    @Test
    void getLatestToken_shouldReturn404WhenNoTokenExists() throws Exception {
        // Given: Không có token nào trong DB
        when(tokenRepository.findTopByOrderByCreatedAtDesc())
                .thenReturn(java.util.Optional.empty());

        // When & Then: API trả về 404
        mockMvc.perform(get("/api/v1/password-reset-tokens/latest"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.data").value("Không tìm thấy token nào trong hệ thống"));
    }
}

