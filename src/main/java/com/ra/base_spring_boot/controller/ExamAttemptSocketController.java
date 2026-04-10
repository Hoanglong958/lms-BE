package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.ChatMessageSocket.ExamMessageDTO;
import com.ra.base_spring_boot.dto.ExamAttempt.ExamAttemptResponseDTO;
import com.ra.base_spring_boot.model.ExamParticipant;
import com.ra.base_spring_boot.services.exam.IExamAttemptService;
import com.ra.base_spring_boot.services.exam.IExamParticipantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/exam-action")
public class ExamAttemptSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final IExamAttemptService examAttemptService;
    private final IExamParticipantService examParticipantService;

    // =================== WEBSOCKET ===================
    @MessageMapping("/exam/action")
    public void examAction(ExamMessageDTO message) {
        process(message);
        // Broadcast đã thực hiện trong process()
    }

    // =================== REST FOR TESTING ===================
    @PostMapping("/action")
    public ResponseEntity<?> actionRest(@RequestBody ExamMessageDTO message) {
        ExamMessageDTO dto = process(message);
        if(dto == null) return ResponseEntity.badRequest().body("Invalid message or type");
        return ResponseEntity.ok(dto);
    }

    // ==================== MAIN LOGIC ====================
    private ExamMessageDTO process(ExamMessageDTO message) {
        if(message == null || message.getType() == null) {
            log.warn("Received null message or type");
            return null;
        }

        ExamMessageDTO dto = new ExamMessageDTO();

        switch (message.getType()) {

            case "JOIN_EXAM" -> {
                ExamParticipant participant = examParticipantService.joinExam(
                        message.getExamId(),
                        message.getUserId(),
                        LocalDateTime.now()
                );

                if(participant == null) {
                    dto.setType("JOIN_EXAM_FAILED");
                    return dto;
                }

                dto.setType("JOIN_EXAM_SUCCESS");
                dto.setUserId(participant.getUser().getId());
                dto.setExamId(participant.getExam().getId());

                messagingTemplate.convertAndSend(
                        "/topic/exam/" + participant.getExam().getId(),
                        dto
                );
            }

            case "SUBMIT_EXAM" -> {
                ExamParticipant participant =
                        examParticipantService.getParticipant(message.getUserId(), message.getExamId());

                if(participant == null) {
                    dto.setType("SUBMIT_EXAM_FAILED");
                    return dto;
                }

                // Tạo attempt → dùng startAttempt() thay cho createAttempt()
                ExamAttemptResponseDTO attemptDto = examAttemptService.startAttempt(
                        participant.getExam().getId(),
                        participant.getUser().getId()
                );

                if(attemptDto == null) {
                    dto.setType("SUBMIT_EXAM_FAILED");
                    return dto;
                }

                // Submit answer
                examAttemptService.submitExam(attemptDto.getId(), message.getAnswers());

                // Chốt attempt
                ExamAttemptResponseDTO finalAttempt = examAttemptService.submitAttempt(attemptDto.getId());

                dto.setType("SUBMIT_EXAM_SUCCESS");
                dto.setAttemptId(finalAttempt.getId());
                dto.setUserId(finalAttempt.getUserId());
                dto.setExamId(finalAttempt.getExamId());
                dto.setStartTime(finalAttempt.getStartTime());
                dto.setEndTime(finalAttempt.getEndTime());
                dto.setScore(finalAttempt.getScore());

                messagingTemplate.convertAndSend(
                        "/topic/exam/" + finalAttempt.getExamId(),
                        dto
                );
            }

            default -> {
                dto.setType("UNKNOWN_TYPE");
                log.warn("Received unknown message type: {}", message.getType());
            }
        }

        return dto;
    }
}
