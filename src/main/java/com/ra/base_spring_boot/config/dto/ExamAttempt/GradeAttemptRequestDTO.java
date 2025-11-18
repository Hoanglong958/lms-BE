package com.ra.base_spring_boot.config.dto.ExamAttempt;

import lombok.Data;

@Data
public class GradeAttemptRequestDTO {
    private Long attemptId;
    private Double score;
}
