package com.ra.base_spring_boot.dto.analytics;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MasteryMetricDTO {
    private String lessonType;
    private double averageProgressPercent;
}
