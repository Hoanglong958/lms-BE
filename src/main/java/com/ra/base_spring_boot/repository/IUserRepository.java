package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IUserRepository extends JpaRepository<User, Long> {
    // ✅ Dùng email để đăng nhập
    Optional<User> findByEmail(String email);

    // ✅ Dùng cho quên mật khẩu
    Optional<User> findByResetToken(String token);

    // ✅ Kiểm tra email tồn tại (đăng ký)
    boolean existsByEmail(String email);
}
