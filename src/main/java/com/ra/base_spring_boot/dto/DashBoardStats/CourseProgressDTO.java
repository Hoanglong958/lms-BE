package com.ra.base_spring_boot.dto.DashBoardStats;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseProgressDTO {
    private Long courseId;
    private long completed;
    private long inProgress;
    private double completionRate; // %
}