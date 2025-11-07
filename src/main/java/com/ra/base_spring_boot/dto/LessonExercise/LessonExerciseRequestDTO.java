package com.ra.base_spring_boot.dto.LessonExercise;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonExerciseRequestDTO {

    @NotNull(message = "lessonId không được để trống")
    private Long lessonId;

    @NotBlank(message = "title không được để trống")
    @Size(max = 200, message = "title không được vượt quá 200 ký tự")
    private String title;

    @NotBlank(message = "instructions không được để trống")
    private String instructions;

    private String requiredFields;
    private String exampleCode;
    private String notes;
}