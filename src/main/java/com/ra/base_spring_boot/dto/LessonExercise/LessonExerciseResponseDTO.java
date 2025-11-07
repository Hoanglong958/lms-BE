package com.ra.base_spring_boot.dto.LessonExercise;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonExerciseResponseDTO {
    private Long exerciseId;
    private Long lessonId;
    private String title;
    private String instructions;
    private String requiredFields;
    private String exampleCode;
    private String notes;
}
