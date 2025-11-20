package com.ra.base_spring_boot.dto.SessionExercise;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionExerciseResponseDTO {
    private Long exerciseId;
    private Long sessionId;
    private String title;
    private String instructions;
    private String requiredFields;
    private String exampleCode;
    private String notes;
}
