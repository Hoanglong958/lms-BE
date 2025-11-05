package com.ra.base_spring_boot.dto.Question;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionResponseDTO {
    private Long id;
    private String category;
    private String questionText;
    private String options;
    private String correctAnswer;
    private String explanation;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
