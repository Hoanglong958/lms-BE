package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.Gmail.EmailDTO;
import com.ra.base_spring_boot.model.constants.EmailType;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendEmail(EmailType emailType, String to, Map<String, Object> variables) {

        Context context = new Context();
        context.setVariables(Objects.requireNonNull(variables, "variables must not be null"));

        String template = switch (emailType) {
            case USER_CREATED -> "user_created";
            case FORGOT_PASSWORD -> "forgot_password";
            case ADDED_TO_CLASS -> "added_to_class";
            case NEW_COURSE_IN_CLASS -> "new_course";
            case NEW_EXAM -> "new_exam";
        };

        String html = templateEngine.process(Objects.requireNonNull(template, "template must not be null"), context);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            if (fromEmail != null && !fromEmail.isBlank()) {
                helper.setFrom(fromEmail);
            }
            helper.setTo(Objects.requireNonNull(to, "to must not be null"));
            helper.setSubject("📢 Thông báo từ hệ thống LMS");
            helper.setText(Objects.requireNonNull(html, "email html must not be null"), true);

            mailSender.send(message);

            // ✅ Logging mail thành công
            System.out.println("📧 Mail đã gửi thành công tới: " + to);
            System.out.println("📄 Template: " + template);
            System.out.println("🕒 Thời gian gửi: " + java.time.LocalDateTime.now());

        } catch (Exception e) {
            // ❌ Logging mail lỗi
            System.err.println("❌ Gửi mail thất bại tới: " + to);
            System.err.println("Template: " + template);
            e.printStackTrace();
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Async
    public void sendEmail(EmailDTO emailDTO) {
        Context context = new Context();
        context.setVariables(Objects.requireNonNull(emailDTO.getTemplateData(), "templateData must not be null"));

        String html = templateEngine.process(Objects.requireNonNull(emailDTO.getTemplateName(), "templateName must not be null"), context);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            if (fromEmail != null && !fromEmail.isBlank()) {
                helper.setFrom(fromEmail);
            }
            helper.setTo(Objects.requireNonNull(emailDTO.getTo(), "to must not be null"));
            helper.setSubject(Objects.requireNonNull(emailDTO.getSubject(), "subject must not be null"));
            helper.setText(Objects.requireNonNull(html, "email html must not be null"), true);

            mailSender.send(message);

            // ✅ Logging mail thành công
            System.out.println("📧 Mail đã gửi thành công tới: " + emailDTO.getTo());
            System.out.println("📄 Template: " + emailDTO.getTemplateName());
            System.out.println("🕒 Thời gian gửi: " + java.time.LocalDateTime.now());

        } catch (Exception e) {
            // ❌ Logging mail lỗi
            System.err.println("❌ Gửi mail thất bại tới: " + emailDTO.getTo());
            System.err.println("Template: " + emailDTO.getTemplateName());
            e.printStackTrace();
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
