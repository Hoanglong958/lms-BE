package com.ra.base_spring_boot.config.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ra.base_spring_boot.dto.req.CreatePasswordResetTokenRequest;
import com.ra.base_spring_boot.dto.req.ResetPasswordRequest;
import com.ra.base_spring_boot.dto.resp.PasswordResetTokenResponse;
import com.ra.base_spring_boot.services.IPasswordResetTokenService;
import com.ra.base_spring_boot.security.principle.MyUserDetailsService;
import com.ra.base_spring_boot.security.jwt.JwtProvider;
import com.ra.base_spring_boot.security.jwt.JwtTokenFilter;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = PasswordResetTokenController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
        }
)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class PasswordResetTokenControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private IPasswordResetTokenService tokenService;
    @MockBean
    private MyUserDetailsService myUserDetailsService;
    @MockBean
    private JwtProvider jwtProvider;
    @MockBean
    private JwtTokenFilter jwtTokenFilter;
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void createToken_shouldReturn201() throws Exception {
        PasswordResetTokenResponse response = PasswordResetTokenResponse.builder()
                .id(1L)
                .userId(5L)
                .token("abc")
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .isUsed(false)
                .createdAt(LocalDateTime.now())
                .build();
        when(tokenService.create(any(CreatePasswordResetTokenRequest.class))).thenReturn(response);

        CreatePasswordResetTokenRequest request = new CreatePasswordResetTokenRequest();
        request.setEmail("user@example.com");

        mockMvc.perform(post("/api/v1/password-reset-tokens/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.token").value("abc"));

        verify(tokenService).create(any(CreatePasswordResetTokenRequest.class));
    }

    @Test
    void resetPassword_shouldReturn200() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("abc");
        request.setNewPassword("new-password");

        mockMvc.perform(post("/api/v1/password-reset-tokens/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk());

        verify(tokenService).resetPassword(any(ResetPasswordRequest.class));
    }

    @Test
    void resetPassword_shouldValidateInput() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("");
        request.setNewPassword("");

        mockMvc.perform(post("/api/v1/password-reset-tokens/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest());
    }
}

