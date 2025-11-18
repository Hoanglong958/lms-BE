package com.ra.base_spring_boot.config.dto.LessonQuizzes;

import lombok.*;

import java.util.List;

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
    private List<LessonQuizResponseDTO> recentQuizzes; // quiz gần đây 30 ngày


}
