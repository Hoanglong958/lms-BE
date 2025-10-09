package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IEntityUserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
}
