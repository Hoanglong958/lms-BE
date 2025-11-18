package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.config.dto.Exam.ExamResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ExamSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    // Trường hợp FE gửi message lên (nếu muốn test)
    @MessageMapping("/exam/notify")
    @SendTo("/topic/exam-updates")
    public ExamResponseDTO broadcastExam(ExamResponseDTO message) {
        System.out.println("Nhận message từ FE: " + message.getTitle());
        return message; // broadcast lại cho toàn bộ FE
    }

    // Gửi thông báo realtime từ Backend (ExamServiceImpl gọi hàm này)
    public void sendExamCreatedNotification(ExamResponseDTO exam) {
        messagingTemplate.convertAndSend("/topic/exam-updates", exam);
    }
}
