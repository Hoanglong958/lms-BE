package com.ra.base_spring_boot.dto.Quiz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuizQuestionRequest {
    private Long quizId;
    private Long questionId;
    private Integer orderIndex;
}
