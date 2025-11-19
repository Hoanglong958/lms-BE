package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.req.UserCreateRequest;
import com.ra.base_spring_boot.dto.req.UserUpdateRequest;
import com.ra.base_spring_boot.dto.resp.UserResponse;
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
        // ===== Validate Full Name =====
        if (req.getFullName() == null || req.getFullName().isBlank()) {
            throw new HttpBadRequest("Họ tên không được để trống!");
        }

        // ===== Validate Gmail =====
        if (req.getGmail() == null || req.getGmail().isBlank()) {
            throw new HttpBadRequest("Gmail không được để trống!");
        }
        if (!req.getGmail().matches("^[A-Za-z0-9._%+-]{6,}@gmail\\.com$")) {
            throw new HttpBadRequest("Gmail phải là gmail.com và phần username trước @ phải trên 5 ký tự!");
        }
        if (userRepository.existsByGmail(req.getGmail())) {
            throw new HttpBadRequest("Gmail đã tồn tại!");
        }

        // ===== Validate Password (strong) =====
        if (req.getPassword() == null || req.getPassword().isBlank()) {
            throw new HttpBadRequest("Mật khẩu không được để trống!");
        }
        String passwordRegex = "^(?=.*[0-9])" +
                "(?=.*[a-z])" +
                "(?=.*[A-Z])" +
                "(?=.*[!@#$%^&*()_+\\-={}\\[\\]|:;\"'<>.,?//])" +
                ".{8,}$";
        if (!req.getPassword().matches(passwordRegex)) {
            throw new HttpBadRequest("Mật khẩu phải có ít nhất 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt!");
        }

        // ===== Validate Role =====
        RoleName role = RoleName.ROLE_USER; // mặc định USER
        if (req.getRole() != null) {
            try {
                String normalizedRole = req.getRole().trim().toUpperCase();
                if (!normalizedRole.startsWith("ROLE_")) {
                    normalizedRole = "ROLE_" + normalizedRole;
                }
                role = RoleName.valueOf(normalizedRole);
            } catch (IllegalArgumentException e) {
                throw new HttpBadRequest("Role không hợp lệ! Chỉ chấp nhận USER, TEACHER hoặc ADMIN.");
            }
        }

        Boolean active = req.getIsActive() != null ? req.getIsActive() : Boolean.TRUE;

        User user = User.builder()
                .fullName(req.getFullName())
                .gmail(req.getGmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .phone(req.getPhone())
                .role(role)
                .isActive(active)
                .createdAt(LocalDateTime.now())
                .build();
        try {
            userRepository.save(user);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new HttpBadRequest("Dữ liệu không hợp lệ hoặc gmail đã tồn tại!");
        }
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
    public boolean gmailExists(String gmail) {
        if (gmail == null || gmail.trim().isEmpty()) {
            throw new HttpBadRequest("Gmail không được để trống");
        }
        String normalized = gmail.trim().toLowerCase();
        return userRepository.existsByGmailIgnoreCase(normalized);
    }

    private UserResponse toResponse(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .fullName(u.getFullName())
                .gmail(u.getGmail())
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
        if ("TEACHER".equals(s) || "ROLE_TEACHER".equals(s)) return RoleName.ROLE_TEACHER;
        return fallback;
    }

    private String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
