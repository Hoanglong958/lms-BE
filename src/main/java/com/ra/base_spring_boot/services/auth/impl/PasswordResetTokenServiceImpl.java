package com.ra.base_spring_boot.services.auth.impl;

import com.ra.base_spring_boot.dto.req.CreatePasswordResetTokenRequest;
import com.ra.base_spring_boot.dto.req.ResetPasswordRequest;
import com.ra.base_spring_boot.dto.resp.PasswordResetTokenResponse;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.PasswordResetToken;
import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.repository.auth.IPasswordResetTokenRepository;
import com.ra.base_spring_boot.repository.user.IUserRepository;
import com.ra.base_spring_boot.services.auth.IPasswordResetTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenServiceImpl implements IPasswordResetTokenService {

    private final IPasswordResetTokenRepository tokenRepository;
    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public PasswordResetTokenResponse create(CreatePasswordResetTokenRequest request) {
        User user = userRepository.findByGmail(java.util.Objects.requireNonNull(request.getGmail(), "gmail must not be null"))
                .orElseThrow(() -> new HttpBadRequest("Gmail không tồn tại trong hệ thống!"));

        tokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        PasswordResetToken entity = PasswordResetToken.builder()
                .user(user)
                .token(token)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .isUsed(false)
                .createdAt(LocalDateTime.now())
                .build();
        entity = tokenRepository.save(java.util.Objects.requireNonNull(entity, "token entity must not be null"));

        return PasswordResetTokenResponse.builder()
                .id(entity.getId())
                .userId(user.getId())
                .token(entity.getToken())
                .expiresAt(entity.getExpiresAt())
                .isUsed(entity.getIsUsed())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    @Override
    public boolean validateToken(String token) {
        return tokenRepository.findByToken(java.util.Objects.requireNonNull(token, "token must not be null"))
                .filter(t -> !Boolean.TRUE.equals(t.getIsUsed()))
                .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()))
                .isPresent();
    }

    @Override
    @Transactional
    public void markUsed(String token) {
        PasswordResetToken t = tokenRepository.findByToken(java.util.Objects.requireNonNull(token, "token must not be null"))
                .orElseThrow(() -> new HttpBadRequest("Token không hợp lệ!"));
        if (Boolean.TRUE.equals(t.getIsUsed())) {
            throw new HttpBadRequest("Token đã được sử dụng!");
        }
        if (t.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new HttpBadRequest("Token đã hết hạn!");
        }
        t.setIsUsed(true);
        tokenRepository.save(java.util.Objects.requireNonNull(t, "token entity must not be null"));
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = tokenRepository.findByToken(java.util.Objects.requireNonNull(request.getToken(), "token must not be null"))
                .orElseThrow(() -> new HttpBadRequest("Token không hợp lệ!"));
        if (Boolean.TRUE.equals(token.getIsUsed())) {
            throw new HttpBadRequest("Token đã được sử dụng!");
        }
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new HttpBadRequest("Token đã hết hạn!");
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(java.util.Objects.requireNonNull(user, "user must not be null"));

        token.setIsUsed(true);
        tokenRepository.save(java.util.Objects.requireNonNull(token, "token entity must not be null"));
    }
}
