package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.Gmail.EmailDTO;
import com.ra.base_spring_boot.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Profile("!dev")
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceNoop implements NotificationService {

    private final GmailService gmailService;

    @Value("${app.notifications.sendOnAdminCreate:false}")
    private boolean sendOnAdminCreate;

    @Override
    public void sendAccountCreatedEmail(String to, String username) {
        if (!sendOnAdminCreate) {
            log.debug("Skipping account created email for {} (sendOnAdminCreate=false)", to);
            return;
        }
        try {
            EmailDTO emailDTO = new EmailDTO(to, "Chào mừng bạn đến với hệ thống", "user_created",
                    Map.of("username", username));
            gmailService.sendEmail(emailDTO);
            log.info("Admin-created account email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send admin-created account email to {}: {}", to, e.getMessage(), e);
        }
    }

    @Override
    public void sendTeacherAccountCreatedEmail(String to, String username, String tempPassword) {
        sendAdminTeacherAccountCreatedEmail(to, username, tempPassword, "Giảng viên");
    }

    @Override
    public void sendAdminTeacherAccountCreatedEmail(String to, String username, String tempPassword,
            String roleDisplayName) {
        if (!sendOnAdminCreate) {
            log.debug("Skipping admin/teacher account email for {} (sendOnAdminCreate=false)", to);
            return;
        }
        try {
            EmailDTO emailDTO = new EmailDTO(
                    to,
                    "Thông báo cấp tài khoản " + roleDisplayName,
                    "admin_teacher_account_created",
                    Map.of("username", username, "roleName", roleDisplayName, "tempPassword", tempPassword, "email",
                            to));
            gmailService.sendEmail(emailDTO);
            log.info("Admin-created admin/teacher account email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send admin/teacher account email to {}: {}", to, e.getMessage(), e);
        }
    }

    @Override
    public void sendForgotPasswordEmail(String to, String resetLink) {
        if (!sendOnAdminCreate) {
            log.debug("Skipping forgot password email (sendOnAdminCreate=false) for {}", to);
            return;
        }
        try {
            EmailDTO emailDTO = new EmailDTO(to, "Đặt lại mật khẩu", "forgot_password", Map.of("resetLink", resetLink));
            gmailService.sendEmail(emailDTO);
            log.info("Forgot-password email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send forgot-password email to {}: {}", to, e.getMessage(), e);
        }
    }

    @Override
    public void sendAddedToClassEmail(String to, String className) {
        if (!sendOnAdminCreate) {
            log.debug("Skipping added-to-class email (sendOnAdminCreate=false) for {}", to);
            return;
        }
        try {
            EmailDTO emailDTO = new EmailDTO(to, "Bạn đã được thêm vào lớp học", "added_to_class",
                    Map.of("className", className));
            gmailService.sendEmail(emailDTO);
            log.info("Added-to-class email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send added-to-class email to {}: {}", to, e.getMessage(), e);
        }
    }

    @Override
    public void sendNewCourseEmail(String to, String courseName, String className) {
        if (!sendOnAdminCreate) {
            log.debug("Skipping new-course email (sendOnAdminCreate=false) for {}", to);
            return;
        }
        try {
            EmailDTO emailDTO = new EmailDTO(to, "Khóa học mới", "new_course",
                    Map.of("courseName", courseName, "className", className));
            gmailService.sendEmail(emailDTO);
            log.info("New-course email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send new-course email to {}: {}", to, e.getMessage(), e);
        }
    }

    @Override
    public void sendNewExamEmail(String to, String examName, String className) {
        if (!sendOnAdminCreate) {
            log.debug("Skipping new-exam email (sendOnAdminCreate=false) for {}", to);
            return;
        }
        try {
            EmailDTO emailDTO = new EmailDTO(to, "Bài kiểm tra mới", "new_exam",
                    Map.of("examName", examName, "className", className));
            gmailService.sendEmail(emailDTO);
            log.info("New-exam email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send new-exam email to {}: {}", to, e.getMessage(), e);
        }
    }
}
