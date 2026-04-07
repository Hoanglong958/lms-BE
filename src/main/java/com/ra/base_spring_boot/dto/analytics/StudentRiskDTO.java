package com.ra.base_spring_boot.dto.analytics;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentRiskDTO {
    private Long studentId;
    private String studentName;
    private double attendanceRate;
    private double progressPercent;
    private double scorePercent;
    private String riskFactors;
}
