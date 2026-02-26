package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.resp.PasswordResetTokenResponse;
import com.ra.base_spring_boot.model.PasswordResetToken;
import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.repository.auth.IPasswordResetTokenRepository;
import com.ra.base_spring_boot.services.auth.IPasswordResetTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import com.ra.base_spring_boot.security.principle.MyUserDetailsService;
import com.ra.base_spring_boot.security.jwt.JwtProvider;
import com.ra.base_spring_boot.security.jwt.JwtTokenFilter;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PasswordResetTokenController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("removal")
class PasswordResetTokenControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IPasswordResetTokenService tokenService;

    @MockBean
    private IPasswordResetTokenRepository tokenRepository;

    @MockBean
    private MyUserDetailsService myUserDetailsService;
    @MockBean
    private JwtProvider jwtProvider;
    @MockBean
    private JwtTokenFilter jwtTokenFilter;
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void validateToken_shouldReturn200WithTrueWhenTokenValid() throws Exception {
        when(tokenService.validateToken("valid-token")).thenReturn(true);

        mockMvc.perform(get("/api/v1/password-reset-tokens/validate")
                .param("token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        verify(tokenService).validateToken("valid-token");
    }

    @Test
    void validateToken_shouldReturn200WithFalseWhenTokenInvalid() throws Exception {
        when(tokenService.validateToken("invalid-token")).thenReturn(false);

        mockMvc.perform(get("/api/v1/password-reset-tokens/validate")
                .param("token", "invalid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    void validateToken_shouldReturn200WithFalseWhenTokenExpired() throws Exception {
        when(tokenService.validateToken("expired-token")).thenReturn(false);

        mockMvc.perform(get("/api/v1/password-reset-tokens/validate")
                .param("token", "expired-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    void validateToken_shouldReturn200WithFalseWhenTokenAlreadyUsed() throws Exception {
        when(tokenService.validateToken("used-token")).thenReturn(false);

        mockMvc.perform(get("/api/v1/password-reset-tokens/validate")
                .param("token", "used-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    void getLatestToken_shouldReturn200WithTokenWhenExists() throws Exception {
        User mockUser = User.builder()
                .id(100L)
                .gmail("test@gmail.com")
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
        when(tokenRepository.findTopByOrderByCreatedAtDesc())
                .thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/api/v1/password-reset-tokens/latest"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.data").value("Không tìm thấy token nào trong hệ thống"));
    }
}
