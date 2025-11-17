package com.ra.base_spring_boot.dto.ExamAttempt;

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
public class SubmitAttemptRequestDTO {
    @NotEmpty
    private List<AnswerItem> answers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnswerItem {
        @NotNull
        private Long questionId;
        @NotNull
        private String answer;
    }
}
