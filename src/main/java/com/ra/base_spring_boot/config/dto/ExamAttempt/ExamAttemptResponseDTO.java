package com.ra.base_spring_boot.config.dto.ExamAttempt;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExamAttemptResponseDTO {
    private Long id;
    private Long examId;
    private Long userId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double score;
    private String status;
    private Integer attemptNumber;
}
