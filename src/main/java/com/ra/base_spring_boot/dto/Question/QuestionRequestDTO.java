package com.ra.base_spring_boot.dto.Question;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class QuestionRequestDTO {
    private String category;
    private String questionText;
    private String options;
    private String correctAnswer;
    private String explanation;
}

