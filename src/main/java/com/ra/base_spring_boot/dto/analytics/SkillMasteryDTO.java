package com.ra.base_spring_boot.dto.analytics;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SkillMasteryDTO {
    private String skillName;
    private double averageProgressPercent;
    private long studentCount;
}
