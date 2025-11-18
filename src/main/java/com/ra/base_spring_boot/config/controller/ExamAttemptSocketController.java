package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.config.dto.ChatMessageSocket.ExamMessageDTO;
import com.ra.base_spring_boot.model.ExamAttempt;
import com.ra.base_spring_boot.model.ExamParticipant;
import com.ra.base_spring_boot.services.IExamAttemptService;
import com.ra.base_spring_boot.services.IExamParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/exam-action")
public class ExamAttemptSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final IExamAttemptService examAttemptService;
    private final IExamParticipantService examParticipantService;

    // ==================== SOCKET ENDPOINT ====================
    @MessageMapping("/exam/action")
    public void examAction(ExamMessageDTO message) {
        process(message);
    }

    // ==================== REST ENDPOINT (for testing) ====================
    @PostMapping("/action")
    public ResponseEntity<?> actionRest(@RequestBody ExamMessageDTO message) {
        return ResponseEntity.ok(process(message));
    }

    // ==================== XỬ LÝ JOIN / SUBMIT ====================
    private ExamMessageDTO process(ExamMessageDTO message) {
        ExamMessageDTO dto = new ExamMessageDTO();

        switch (message.getType()) {

            case "JOIN_EXAM" -> {
                // 1️⃣ Tạo participant thực tế nếu chưa có
                ExamParticipant participant = examParticipantService.joinExam(
                        message.getUserId(),
                        Long.valueOf(message.getExamRoomId()),
                        LocalDateTime.now()
                );

                // 2️⃣ Tạo DTO gửi FE
                dto.setType("JOIN_EXAM_SUCCESS");
                dto.setUserId(participant.getUser().getId());
                dto.setExamRoomId(participant.getExamRoomId().toString());
                dto.setExamId(participant.getExam().getId());

                // 3️⃣ Broadcast realtime cho tất cả user trong phòng
                messagingTemplate.convertAndSend(
                        "/topic/exam-room/" + participant.getExamRoomId(), dto
                );
            }

            case "SUBMIT_EXAM" -> {
                // 1️⃣ Lấy participant đã join
                ExamParticipant participant = examParticipantService.getParticipant(
                        message.getUserId(),
                        Long.valueOf(message.getExamRoomId())
                );

                // 2️⃣ Tạo attempt mới
                ExamAttempt attempt = examAttemptService.createAttempt(
                        participant.getExam().getId(),
                        participant.getUser().getId(),
                        participant.getExamRoomId()
                );

                // 3️⃣ Submit attempt với câu trả lời từ FE
                attempt = examAttemptService.submitExam(
                        attempt.getId(),
                        message.getAnswers() // Map<Long, String>
                );

                // 4️⃣ Tạo DTO gửi FE
                dto.setType("SUBMIT_EXAM_SUCCESS");
                dto.setAttemptId(attempt.getId());
                dto.setUserId(attempt.getUser().getId());
                dto.setExamId(attempt.getExam().getId());
                dto.setExamRoomId(participant.getExamRoomId().toString());
                dto.setStartTime(attempt.getStartTime());
                dto.setEndTime(attempt.getEndTime());

                // 5️⃣ Broadcast realtime cho tất cả user trong phòng
                messagingTemplate.convertAndSend(
                        "/topic/exam-room/" + participant.getExamRoomId(), dto
                );
            }

            default -> dto.setType("UNKNOWN_TYPE");
        }

        return dto;
    }
}
