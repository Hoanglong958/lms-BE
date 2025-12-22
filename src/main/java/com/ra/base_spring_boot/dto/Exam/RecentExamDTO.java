package com.ra.base_spring_boot.dto.Exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecentExamDTO {
    private Long id;
    private String title;
    private Integer maxScore;
    private Integer passingScore;
    private Long attempts;
    private Double passRate;
    private LocalDateTime createdAt;
}
