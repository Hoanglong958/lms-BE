package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.PasswordResetOtp;
import com.ra.base_spring_boot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface IPasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {
    Optional<PasswordResetOtp> findTopByUserOrderByCreatedAtDesc(User user);
    void deleteByUser(User user);
    long deleteByExpiresAtBefore(LocalDateTime cutoff);
}
