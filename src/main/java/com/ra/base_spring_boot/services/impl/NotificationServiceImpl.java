package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.Gmail.EmailDTO;
import com.ra.base_spring_boot.services.EmailProducerService;
import com.ra.base_spring_boot.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.profiles.active", havingValue = "dev", matchIfMissing = false)
public class NotificationServiceImpl implements NotificationService {
    
    private final EmailProducerService emailProducerService;

    @Override
    public void sendAccountCreatedEmail(String to, String username) {
        emailProducerService.pushEmailToQueue(
            new EmailDTO(to, "Chào mừng bạn đến với hệ thống", "user_created", 
                Map.of("username", username))
        );
    }

    @Override
    public void sendForgotPasswordEmail(String to, String resetLink) {
        emailProducerService.pushEmailToQueue(
            new EmailDTO(to, "Đặt lại mật khẩu", "forgot_password", 
                Map.of("resetLink", resetLink))
        );
    }

    @Override
    public void sendAddedToClassEmail(String to, String className) {
        emailProducerService.pushEmailToQueue(
            new EmailDTO(to, "Bạn đã được thêm vào lớp học", "added_to_class", 
                Map.of("className", className))
        );
    }

    @Override
    public void sendNewCourseEmail(String to, String courseName, String className) {
        emailProducerService.pushEmailToQueue(
            new EmailDTO(to, "Khóa học mới", "new_course", 
                Map.of("courseName", courseName, "className", className))
        );
    }

    @Override
    public void sendNewExamEmail(String to, String examName, String className) {
        emailProducerService.pushEmailToQueue(
            new EmailDTO(to, "Bài kiểm tra mới", "new_exam", 
                Map.of("examName", examName, "className", className))
        );
    }
    
    @Override
    public void sendTeacherAccountCreatedEmail(String to, String username, String tempPassword) {
        emailProducerService.pushEmailToQueue(
            new EmailDTO(to, "Thông báo tạo tài khoản giảng viên", "teacher_account_created", 
                Map.of(
                    "username", username,
                    "tempPassword", tempPassword,
                    "email", to
                ))
        );
    }
}
