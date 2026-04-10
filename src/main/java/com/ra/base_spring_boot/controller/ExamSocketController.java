package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.Exam.ExamResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ExamSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    // Trường hợp FE gửi message lên (nếu muốn test)
    @MessageMapping("/exam/notify")
    @SendTo("/topic/exam-updates")
    public ExamResponseDTO broadcastExam(ExamResponseDTO message) {
        log.info("Nhận message từ FE: {}", message.getTitle());
        return message; // broadcast lại cho toàn bộ FE
    }

    // Gửi thông báo realtime từ Backend (ExamServiceImpl gọi hàm này)
    public void sendExamCreatedNotification(ExamResponseDTO exam) {
        messagingTemplate.convertAndSend("/topic/exam-updates", Objects.requireNonNull(exam));
    }
}
