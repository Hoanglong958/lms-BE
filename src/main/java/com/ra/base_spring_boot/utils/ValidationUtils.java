package com.ra.base_spring_boot.utils;

import com.ra.base_spring_boot.model.constants.RoleName;
import java.util.regex.Pattern;

public class ValidationUtils {

    private static final Pattern GMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]{6,}@gmail\\.com$");
    
    // Mật khẩu ít nhất 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt
    private static final Pattern STRONG_PASSWORD_PATTERN = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-={}\\[\\]|:;\"'<>.,?//]).{8,}$");

    /**
     * Kiểm tra định dạng Gmail (theo yêu cầu hệ thống)
     */
    public static boolean isValidGmail(String gmail) {
        if (gmail == null) return false;
        return GMAIL_PATTERN.matcher(gmail).matches();
    }

    /**
     * Kiểm tra độ mạnh mật khẩu
     */
    public static boolean isStrongPassword(String password) {
        if (password == null) return false;
        return STRONG_PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Chuyển đổi chuỗi role thành RoleName enum
     */
    public static RoleName parseRoleOrDefault(String input, RoleName fallback) {
        if (input == null || input.trim().isEmpty()) return fallback;
        
        String s = input.trim().toUpperCase();
        if (!s.startsWith("ROLE_")) {
            s = "ROLE_" + s;
        }
        
        try {
            return RoleName.valueOf(s);
        } catch (IllegalArgumentException e) {
            // Thử bỏ prefix nếu có lỗi (để chắc chắn)
            try {
                return RoleName.valueOf("ROLE_" + input.trim().toUpperCase());
            } catch (Exception ex) {
                return fallback;
            }
        }
    }
}
