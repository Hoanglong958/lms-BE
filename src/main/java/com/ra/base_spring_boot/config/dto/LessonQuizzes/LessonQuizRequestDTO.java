package com.ra.base_spring_boot.config.dto.LessonQuizzes;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonQuizRequestDTO {
    private Long lessonId;       // ID bài học chứa quiz
    private String title;        // Tên quiz
    private Integer questionCount;
    private Integer maxScore;
    private Integer passingScore;
}
