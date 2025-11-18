package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.config.dto.req.UserCreateRequest;
import com.ra.base_spring_boot.config.dto.req.UserUpdateRequest;
import com.ra.base_spring_boot.config.dto.resp.UserResponse;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.exception.HttpNotFound;
import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.model.constants.RoleName;
import com.ra.base_spring_boot.repository.IUserRepository;
import com.ra.base_spring_boot.services.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<UserResponse> search(String keyword, RoleName role, Boolean isActive, Pageable pageable) {
        return userRepository.search(emptyToNull(keyword), role, isActive, pageable)
                .map(this::toResponse);
    }

    @Override
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("User không tồn tại"));
        return toResponse(user);
    }

    @Override
    public UserResponse create(UserCreateRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new HttpBadRequest("Email đã tồn tại!");
        }
        RoleName role = parseRoleOrDefault(req.getRole(), RoleName.ROLE_USER);
        Boolean active = req.getIsActive() != null ? req.getIsActive() : Boolean.TRUE;
        User user = User.builder()
                .fullName(req.getFullName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(role)
                .isActive(active)
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(user);
        return toResponse(user);
    }

    @Override
    public UserResponse update(Long id, UserUpdateRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("User không tồn tại"));
        if (req.getFullName() != null) {
            user.setFullName(req.getFullName());
        }
        if (req.getRole() != null) {
            user.setRole(parseRoleOrDefault(req.getRole(), user.getRole()));
        }
        if (req.getIsActive() != null) {
            user.setIsActive(req.getIsActive());
        }
        userRepository.save(user);
        return toResponse(user);
    }

    @Override
    public void softDelete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("User không tồn tại"));
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Override
    public void toggleStatus(Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("User không tồn tại"));
        user.setIsActive(active);
        userRepository.save(user);
    }

    @Override
    public boolean emailExists(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new HttpBadRequest("Email không được để trống");
        }
        String normalized = email.trim().toLowerCase();
        return userRepository.existsByEmailIgnoreCase(normalized);
    }

    private UserResponse toResponse(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .role(u.getRole())
                .isActive(u.getIsActive())
                .createdAt(u.getCreatedAt())
                .build();
    }

    private RoleName parseRoleOrDefault(String input, RoleName fallback) {
        if (input == null) return fallback;
        String s = input.trim().toUpperCase();
        if ("ADMIN".equals(s) || "ROLE_ADMIN".equals(s)) return RoleName.ROLE_ADMIN;
        if ("USER".equals(s) || "ROLE_USER".equals(s)) return RoleName.ROLE_USER;
        return fallback;
    }

    private String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
