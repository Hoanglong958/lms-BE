package com.ra.base_spring_boot.dto.Question;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionRequestDTO {
    private String category;

    @NotBlank(message = "Question text is required")
    private String questionText;

    private List<String> options;// Map<String, String> từ frontend

    @NotBlank(message = "Correct answer is required")
    private String correctAnswer;

    private String explanation;
}
