package com.ra.base_spring_boot.dto.ChatMessageSocket;


import com.ra.base_spring_boot.model.ExamAttempt;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ExamMessageDTO {

    private Long attemptId;       // id của attempt
    private String examRoomId;
    private Long examId;          // lấy từ attempt.getExam().getId()
    private Long userId;          // lấy từ attempt.getUser().getId()

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer correctCount;
    private Double score;
    private String status;        // IN_PROGRESS / FINISHED

    private Map<Long, String> answers; // questionId -> selectedAnswer
    private String type;                 // JOIN_EXAM / SUBMIT_EXAM / FINISH_EXAM

    public static ExamMessageDTO fromAttempt(ExamAttempt attempt, String type, String examRoomId) {
        ExamMessageDTO dto = new ExamMessageDTO();
        dto.setAttemptId(attempt.getId());
        dto.setExamRoomId(examRoomId);
        dto.setExamId(attempt.getExam().getId());
        dto.setUserId(attempt.getUser().getId());
        dto.setType(type);
        return dto;
    }

}


