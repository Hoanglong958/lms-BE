package com.ra.base_spring_boot.dto.ExamAttempt;

import lombok.Data;

@Data
public class StartAttemptRequestDTO {
    private Long examId;
    private Long userId;
}
