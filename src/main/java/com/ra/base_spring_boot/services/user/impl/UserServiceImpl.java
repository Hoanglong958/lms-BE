package com.ra.base_spring_boot.services.user.impl;

import com.ra.base_spring_boot.dto.req.UserCreateRequest;
import com.ra.base_spring_boot.dto.req.UserUpdateRequest;
import com.ra.base_spring_boot.dto.resp.UserResponse;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.exception.HttpNotFound;
import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.model.constants.RoleName;
import com.ra.base_spring_boot.repository.user.IUserRepository;
import com.ra.base_spring_boot.services.user.IUserService;
import com.ra.base_spring_boot.services.notification.NotificationService;
import com.ra.base_spring_boot.utils.ValidationUtils;
import com.ra.base_spring_boot.utils.StringUtils;
import com.ra.base_spring_boot.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    @Override
    public Page<UserResponse> search(String keyword, RoleName role, Boolean isActive, Pageable pageable) {
        return userRepository.search(StringUtils.emptyToNull(keyword), role, isActive, pageable)
                .map(this::toResponse);
    }

    @Override
    public UserResponse getById(Long id) {
        User user = userRepository.findById(java.util.Objects.requireNonNull(id, "id must not be null"))
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
        if (!ValidationUtils.isValidGmail(req.getGmail())) {
            throw new HttpBadRequest("Gmail không hợp lệ hoặc không đúng định dạng gmail.com!");
        }
        if (userRepository.existsByGmail(req.getGmail())) {
            throw new HttpBadRequest("Gmail đã tồn tại!");
        }

        // ===== Validate Password (strong) =====
        if (!ValidationUtils.isStrongPassword(req.getPassword())) {
            throw new HttpBadRequest(
                    "Mật khẩu phải có ít nhất 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt!");
        }

        // ===== Validate Role =====
        RoleName role = ValidationUtils.parseRoleOrDefault(req.getRole(), RoleName.ROLE_USER);

        Boolean active = req.getIsActive() != null ? req.getIsActive() : Boolean.TRUE;

        User user = User.builder()
                .fullName(req.getFullName())
                .gmail(req.getGmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .phone(req.getPhone())
                .avatar(req.getAvatar())
                .role(role)
                .isActive(active)
                .createdAt(LocalDateTime.now())
                .build();

        try {
            user = userRepository.save(java.util.Objects.requireNonNull(user, "user must not be null"));

            // Gửi email thông báo tạo tài khoản
            try {
                if (role == RoleName.ROLE_TEACHER || role == RoleName.ROLE_ADMIN) {
                    String roleDisplayName = (role == RoleName.ROLE_TEACHER) ? "Giảng viên" : "Quản trị viên";
                    notificationService.sendAdminTeacherAccountCreatedEmail(user.getGmail(), user.getFullName(),
                            req.getPassword(), roleDisplayName);
                } else {
                    notificationService.sendAccountCreatedEmail(user.getGmail(), user.getFullName());
                }
            } catch (Exception e) {
                // Log lỗi nhưng không ảnh hưởng đến quá trình tạo user
                System.err.println("Không thể gửi email thông báo tạo tài khoản: " + e.getMessage());
                e.printStackTrace();
            }

        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new HttpBadRequest("Dữ liệu không hợp lệ hoặc gmail đã tồn tại!");
        }

        return toResponse(user);
    }

    @Override
    public UserResponse update(Long id, UserUpdateRequest req) {
        User user = userRepository.findById(java.util.Objects.requireNonNull(id, "id must not be null"))
                .orElseThrow(() -> new HttpNotFound("User không tồn tại"));
        if (req.getFullName() != null) {
            user.setFullName(req.getFullName());
        }
        if (req.getGmail() != null && !req.getGmail().isBlank() && !req.getGmail().equals(user.getGmail())) {
            if (!ValidationUtils.isValidGmail(req.getGmail())) {
                throw new HttpBadRequest("Gmail không hợp lệ!");
            }
            if (userRepository.existsByGmail(req.getGmail())) {
                throw new HttpBadRequest("Gmail đã tồn tại!");
            }
            user.setGmail(req.getGmail());
        }
        if (req.getAvatar() != null) {
            user.setAvatar(req.getAvatar());
        }
        if (req.getRole() != null) {
            user.setRole(ValidationUtils.parseRoleOrDefault(req.getRole(), user.getRole()));
        }
        if (req.getIsActive() != null) {
            user.setIsActive(req.getIsActive());
        }
        userRepository.save(java.util.Objects.requireNonNull(user, "user must not be null"));
        return toResponse(user);
    }

    @Override
    public void softDelete(Long id) {
        User user = userRepository.findById(java.util.Objects.requireNonNull(id, "id must not be null"))
                .orElseThrow(() -> new HttpNotFound("User không tồn tại"));
        user.setIsActive(false);
        userRepository.save(java.util.Objects.requireNonNull(user, "user must not be null"));
    }

    @Override
    public void toggleStatus(Long id, boolean active) {
        User user = userRepository.findById(java.util.Objects.requireNonNull(id, "id must not be null"))
                .orElseThrow(() -> new HttpNotFound("User không tồn tại"));
        user.setIsActive(active);
        userRepository.save(java.util.Objects.requireNonNull(user, "user must not be null"));
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
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .fullName(u.getFullName())
                .gmail(u.getGmail())
                .phone(u.getPhone())
                .role(u.getRole())
                .avatar(u.getAvatar())
                .isActive(u.getIsActive())
                .createdAt(u.getCreatedAt())
                .build();
    }

    // Local helpers removed in favor of utilities
}
