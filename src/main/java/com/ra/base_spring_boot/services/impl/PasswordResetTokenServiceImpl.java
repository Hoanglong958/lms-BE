package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.req.CreatePasswordResetTokenRequest;
import com.ra.base_spring_boot.dto.resp.PasswordResetTokenResponse;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.PasswordResetToken;
import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.repository.IPasswordResetTokenRepository;
import com.ra.base_spring_boot.repository.IUserRepository;
import com.ra.base_spring_boot.services.IPasswordResetTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenServiceImpl implements IPasswordResetTokenService {

    private final IPasswordResetTokenRepository tokenRepository;
    private final IUserRepository userRepository;

    @Override
    public PasswordResetTokenResponse create(CreatePasswordResetTokenRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new HttpBadRequest("Email không tồn tại trong hệ thống!"));

        tokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        PasswordResetToken entity = PasswordResetToken.builder()
                .user(user)
                .token(token)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .isUsed(false)
                .createdAt(LocalDateTime.now())
                .build();
        entity = tokenRepository.save(entity);

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
        return tokenRepository.findByToken(token)
                .filter(t -> !Boolean.TRUE.equals(t.getIsUsed()))
                .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()))
                .isPresent();
    }

    @Override
    public void markUsed(String token) {
        PasswordResetToken t = tokenRepository.findByToken(token)
                .orElseThrow(() -> new HttpBadRequest("Token không hợp lệ!"));
        if (Boolean.TRUE.equals(t.getIsUsed())) {
            throw new HttpBadRequest("Token đã được sử dụng!");
        }
        if (t.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new HttpBadRequest("Token đã hết hạn!");
        }
        t.setIsUsed(true);
        tokenRepository.save(t);
    }
}
