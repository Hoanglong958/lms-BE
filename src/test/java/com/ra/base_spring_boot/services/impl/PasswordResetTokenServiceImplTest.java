package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.req.ResetPasswordRequest;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.PasswordResetToken;
import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.repository.IPasswordResetTokenRepository;
import com.ra.base_spring_boot.repository.IUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetTokenServiceImplTest {

    @Mock
    private IPasswordResetTokenRepository tokenRepository;
    @Mock
    private IUserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetTokenServiceImpl passwordResetTokenService;

    @Test
    void resetPassword_shouldEncodePasswordAndMarkTokenUsed() {
        User user = User.builder().id(1L).password("old").build();
        PasswordResetToken token = PasswordResetToken.builder()
                .token("abc")
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .isUsed(false)
                .build();

        when(tokenRepository.findByToken("abc")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("new-pass")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("abc");
        request.setNewPassword("new-pass");

        passwordResetTokenService.resetPassword(request);

        assertThat(user.getPassword()).isEqualTo("encoded");
        assertThat(token.getIsUsed()).isTrue();

        verify(userRepository).save(user);
        verify(tokenRepository).save(token);
    }

    @Test
    void resetPassword_shouldThrowWhenTokenExpired() {
        PasswordResetToken token = PasswordResetToken.builder()
                .token("expired")
                .user(User.builder().id(1L).build())
                .expiresAt(LocalDateTime.now().minusSeconds(1))
                .isUsed(false)
                .build();
        when(tokenRepository.findByToken("expired")).thenReturn(Optional.of(token));

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("expired");
        request.setNewPassword("new");

        assertThrows(HttpBadRequest.class, () -> passwordResetTokenService.resetPassword(request));

        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).save(token);
    }
}

