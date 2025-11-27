package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.Gmail.EmailDTO;
import com.ra.base_spring_boot.model.constants.EmailType;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class GmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Async
    public void sendEmail(EmailType emailType, String to, Map<String, Object> variables) {

        Context context = new Context();
        context.setVariables(variables);

        String template = switch (emailType) {
            case USER_CREATED -> "user_created";
            case FORGOT_PASSWORD -> "forgot_password";
            case ADDED_TO_CLASS -> "added_to_class";
            case NEW_COURSE_IN_CLASS -> "new_course";
            case NEW_EXAM -> "new_exam";
        };

        String html = templateEngine.process(template, context);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject("📢 Thông báo từ hệ thống LMS");
            helper.setText(html, true);

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
        context.setVariables(emailDTO.getTemplateData());

        String html = templateEngine.process(emailDTO.getTemplateName(), context);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(emailDTO.getTo());
            helper.setSubject(emailDTO.getSubject());
            helper.setText(html, true);

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
