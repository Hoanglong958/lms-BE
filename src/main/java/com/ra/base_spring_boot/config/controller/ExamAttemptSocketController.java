package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.dto.ChatMessageSocket.ExamMessageDTO;
import com.ra.base_spring_boot.model.ExamAttempt;
import com.ra.base_spring_boot.services.IExamAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ExamAttemptSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final IExamAttemptService examAttemptService;

    @MessageMapping("/exam/action") // client gửi: /app/exam/action
    public void handleExamAction(ExamMessageDTO message) {

        switch (message.getType()) {

            case "JOIN_EXAM" -> {
                ExamAttempt attempt = examAttemptService.createAttempt(
                        message.getExamId(),
                        message.getUserId(),
                        message.getExamRoomId()
                );

                ExamMessageDTO dto = new ExamMessageDTO();
                dto.setType("JOIN_EXAM_SUCCESS");
                dto.setExamRoomId(message.getExamRoomId());
                dto.setAttemptId(attempt.getId());
                dto.setUserId(attempt.getUser().getId());
                dto.setExamId(attempt.getExam().getId());
                dto.setStartTime(attempt.getStartTime()); // thời gian join

                messagingTemplate.convertAndSend(
                        "/topic/exam-room/" + message.getExamRoomId(),
                        dto
                );
            }

            case "SUBMIT_EXAM" -> {
                ExamAttempt attempt = examAttemptService.submitExam(
                        message.getAttemptId(),
                        message.getAnswers()
                );

                ExamMessageDTO dto = new ExamMessageDTO();
                dto.setType("SUBMIT_EXAM_SUCCESS");
                dto.setExamRoomId(message.getExamRoomId());
                dto.setAttemptId(attempt.getId());
                dto.setUserId(attempt.getUser().getId());
                dto.setExamId(attempt.getExam().getId());
                dto.setStartTime(attempt.getStartTime()); // thời gian join
                dto.setEndTime(attempt.getEndTime());     // thời gian submit

                messagingTemplate.convertAndSend(
                        "/topic/exam-room/" + message.getExamRoomId(),
                        dto
                );
            }

        }
    }
}