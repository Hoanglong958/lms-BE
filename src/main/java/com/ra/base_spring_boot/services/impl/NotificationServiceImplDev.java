package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.Gmail.EmailDTO;
import com.ra.base_spring_boot.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.profiles.active", havingValue = "dev")
public class NotificationServiceImplDev implements NotificationService {

    private final GmailService gmailService;

    @Override
    public void sendAccountCreatedEmail(String to, String username) {
        try {
            EmailDTO emailDTO = new EmailDTO(
                    to,
                    "Chào mừng bạn đến với hệ thống",
                    "user_created",
                    java.util.Map.of("username", username));
            gmailService.sendEmail(emailDTO);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage(), e);
        }
    }

    @Override
    public void sendTeacherAccountCreatedEmail(String to, String username, String tempPassword) {
        sendAdminTeacherAccountCreatedEmail(to, username, tempPassword, "Giảng viên");
    }

    @Override
    public void sendAdminTeacherAccountCreatedEmail(String to, String username, String tempPassword,
            String roleDisplayName) {
        try {
            EmailDTO emailDTO = new EmailDTO(
                    to,
                    "Thông báo cấp tài khoản " + roleDisplayName,
                    "admin_teacher_account_created",
                    java.util.Map.of(
                            "username", username,
                            "roleName", roleDisplayName,
                            "tempPassword", tempPassword,
                            "email", to));
            gmailService.sendEmail(emailDTO);
            log.info("Admin/Teacher account creation email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send admin/teacher account creation email: {}", e.getMessage(), e);
        }
    }

    @Override
    public void sendForgotPasswordEmail(String to, String resetLink) {
        try {
            EmailDTO emailDTO = new EmailDTO(
                    to,
                    "Đặt lại mật khẩu",
                    "forgot_password",
                    java.util.Map.of("resetLink", resetLink));
            gmailService.sendEmail(emailDTO);
            log.info("Password reset email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send password reset email: {}", e.getMessage(), e);
        }
    }

    @Override
    public void sendAddedToClassEmail(String to, String className) {
        try {
            EmailDTO emailDTO = new EmailDTO(
                    to,
                    "Bạn đã được thêm vào lớp học",
                    "added_to_class",
                    java.util.Map.of("className", className));
            gmailService.sendEmail(emailDTO);
            log.info("Added to class email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send added to class email: {}", e.getMessage(), e);
        }
    }

    @Override
    public void sendNewCourseEmail(String to, String courseName, String className) {
        try {
            EmailDTO emailDTO = new EmailDTO(
                    to,
                    "Khóa học mới",
                    "new_course",
                    java.util.Map.of("courseName", courseName, "className", className));
            gmailService.sendEmail(emailDTO);
            log.info("New course email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send new course email: {}", e.getMessage(), e);
        }
    }

    @Override
    public void sendNewExamEmail(String to, String examName, String className) {
        try {
            EmailDTO emailDTO = new EmailDTO(
                    to,
                    "Bài kiểm tra mới",
                    "new_exam",
                    java.util.Map.of("examName", examName, "className", className));
            gmailService.sendEmail(emailDTO);
            log.info("New exam email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send new exam email: {}", e.getMessage(), e);
        }
    }
}
