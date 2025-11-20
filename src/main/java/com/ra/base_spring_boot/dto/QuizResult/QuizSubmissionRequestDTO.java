package com.ra.base_spring_boot.dto.QuizResult;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizSubmissionRequestDTO {
    @NotNull
    private Long quizId;

    @NotNull
    private Long userId;

    @NotEmpty
    @Valid
    private List<AnswerItem> answers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnswerItem {
        @NotNull
        private Long questionId;

        @NotBlank
        private String answer;
    }
}

