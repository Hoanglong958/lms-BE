package com.ra.base_spring_boot.services.notification;

public interface NotificationService {
    void sendAccountCreatedEmail(String to, String username);

    void sendTeacherAccountCreatedEmail(String to, String username, String tempPassword);

    void sendAdminTeacherAccountCreatedEmail(String to, String username, String tempPassword, String roleDisplayName);

    void sendForgotPasswordEmail(String to, String resetLink);

    void sendAddedToClassEmail(String to, String className);

    void sendNewCourseEmail(String to, String courseName, String className);

    void sendNewExamEmail(String to, String examName, String className);
}
