package com.ra.base_spring_boot.dto.LessonQuizzes;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonQuizResponseDTO {
    private Long quizId;
    private Long lessonId;
    private String lessonTitle;
    private String title;
    private Integer questionCount;
    private Integer maxScore;
    private Integer passingScore;
}
