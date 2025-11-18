package com.ra.base_spring_boot.config.dto.Exam;

import com.ra.base_spring_boot.config.dto.Question.QuestionResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ExamResponseDTO {

    private Long id;
    private Long courseId;
    private String title;
    private String description;
    private Integer totalQuestions;
    private Integer maxScore;
    private Integer passingScore;
    private Integer durationMinutes;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<QuestionResponseDTO> questions;


}
