package com.ra.base_spring_boot.dto.Quiz;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class QuizAttemptResponse {
    @Schema(example = "123")
    private Long attemptId;

    @Schema(example = "10")
    private Long quizId;

    @Schema(example = "Kiểm tra kiến thức Java Core")
    private String quizTitle;

    @Schema(example = "1001")
    private Long userId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private BigDecimal score;
    private Integer correctCount;
    private Integer totalCount;
    private Boolean passed;

    private String status; // IN_PROGRESS, SUBMITTED, GRADED
    private Integer attemptNumber;
    private Integer timeSpentSeconds;
    private LocalDateTime createdAt;
}
