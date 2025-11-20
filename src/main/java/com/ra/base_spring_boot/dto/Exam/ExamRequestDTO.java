package com.ra.base_spring_boot.dto.Exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ExamRequestDTO {

    private Long courseId;
    private String title;
    private String description;
    private Integer totalQuestions;
    private Integer maxScore;
    private Integer passingScore;
    private Integer durationMinutes;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean autoAddQuestions; // nếu true lấy tất cả
    private List<Long> questionIds;


}
