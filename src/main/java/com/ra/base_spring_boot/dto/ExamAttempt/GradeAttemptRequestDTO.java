package com.ra.base_spring_boot.dto.ExamAttempt;

import lombok.Data;

@Data
public class GradeAttemptRequestDTO {
    private Long attemptId;
    private Double score;
}
