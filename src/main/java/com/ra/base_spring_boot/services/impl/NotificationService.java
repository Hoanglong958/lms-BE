package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.Gmail.EmailDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailProducerService emailProducerService;

    public void sendAccountCreatedEmail(String to, String username) {
        emailProducerService.pushEmailToQueue(
                new EmailDTO(to, "Chào mừng bạn", "user_created.html", Map.of("username", username))
        );
    }

    public void sendForgotPasswordEmail(String to, String resetLink) {
        emailProducerService.pushEmailToQueue(
                new EmailDTO(to, "Reset mật khẩu", "forgot_password.html", Map.of("resetLink", resetLink))
        );
    }

    public void sendAddedToClassEmail(String to, String className) {
        emailProducerService.pushEmailToQueue(
                new EmailDTO(to, "Bạn đã được thêm vào lớp", "added_to_class.html", Map.of("className", className))
        );
    }

    public void sendNewCourseEmail(String to, String courseName, String className) {
        emailProducerService.pushEmailToQueue(
                new EmailDTO(to, "Khóa học mới được thêm", "new_course.html", Map.of("courseName", courseName, "className", className))
        );
    }

    public void sendNewExamEmail(String to, String examName, String className) {
        emailProducerService.pushEmailToQueue(
                new EmailDTO(to, "Bài thi mới", "new_exam.html", Map.of("examName", examName, "className", className))
        );
    }
}
