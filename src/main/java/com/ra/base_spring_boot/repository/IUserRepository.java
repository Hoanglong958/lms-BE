package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.User;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IUserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);


    boolean existsByUsername(@NotBlank(message = "Không được để trống") String username);

    boolean existsByEmail(@NotBlank(message = "Không được để trống") String email);
}