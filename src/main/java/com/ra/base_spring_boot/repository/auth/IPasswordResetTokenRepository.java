package com.ra.base_spring_boot.repository.auth;

import com.ra.base_spring_boot.model.PasswordResetToken;
import com.ra.base_spring_boot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface IPasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUser(User user);
    long deleteByExpiresAtBefore(LocalDateTime cutoff);
    
    // Dev endpoint: Lấy token mới nhất để test (không cần frontend/email)
    Optional<PasswordResetToken> findTopByOrderByCreatedAtDesc();
}
