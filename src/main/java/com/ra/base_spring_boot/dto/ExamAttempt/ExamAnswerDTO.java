package com.ra.base_spring_boot.dto.ExamAttempt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamAnswerDTO {
    private Long id;
    private Long attemptId;
    private Long questionId;
    private String selectedAnswer;
    private Boolean isCorrect;
    private Integer scoreAwarded;
}


