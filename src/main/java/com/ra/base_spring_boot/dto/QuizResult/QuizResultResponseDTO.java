package com.ra.base_spring_boot.dto.QuizResult;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class QuizResultResponseDTO {
    private Long id;
    private Long quizId;
    private Long userId;
    private Integer correctCount;
    private Integer totalCount;
    private Integer score;
    private Boolean isPassed;
    private LocalDateTime submittedAt;
}
