package com.ra.base_spring_boot.dto.DashBoardStats;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuizReportDTO {
    private Long quizId;
    private String title;
    private long attempts;
    private double avgScore;
    private double passRate; // %
}
