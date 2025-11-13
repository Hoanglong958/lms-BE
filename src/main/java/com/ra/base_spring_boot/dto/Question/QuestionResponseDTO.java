package com.ra.base_spring_boot.dto.Question;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionResponseDTO {
    private Long id;
    private String category;
    private String questionText;
    private List<String> options; // Map để frontend dễ dùng
    private String correctAnswer;
    private String explanation;
    private Double score;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
