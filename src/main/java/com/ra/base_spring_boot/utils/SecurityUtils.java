package com.ra.base_spring_boot.utils;

import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.model.constants.RoleName;
import com.ra.base_spring_boot.repository.user.IUserRepository;
import com.ra.base_spring_boot.security.principle.MyUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

public class SecurityUtils {

    /**
     * Lấy ID của người dùng hiện tại từ SecurityContext
     */
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof MyUserDetails) {
            return ((MyUserDetails) auth.getPrincipal()).getUser().getId();
        }
        return null;
    }

    /**
     * Lấy thông tin User entity của người dùng hiện tại
     */
    public static Optional<User> getCurrentUser(IUserRepository userRepository) {
        Long userId = getCurrentUserId();
        if (userId == null) return Optional.empty();
        return userRepository.findById(userId);
    }

    /**
     * Kiểm tra xem người dùng hiện tại có quyên Admin không
     */
    public static boolean isAdmin() {
        return hasRole(RoleName.ROLE_ADMIN);
    }

    /**
     * Kiểm tra xem người dùng hiện tại có quyền Giáo viên không
     */
    public static boolean isTeacher() {
        return hasRole(RoleName.ROLE_TEACHER);
    }

    /**
     * Kiểm tra xem người dùng hiện tại có quyền User không
     */
    public static boolean isUser() {
        return hasRole(RoleName.ROLE_USER);
    }

    /**
     * Kiểm tra quyền bất kỳ
     */
    public static boolean hasRole(RoleName role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role.name()::equals);
    }
}
