package com.ra.base_spring_boot.dto.UserProgress;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserCourseProgressRequestDTO {

    @Schema(example = "5")
    private Long userId;

    @Schema(example = "3")
    private Long courseId;

    @Schema(example = "10.50")
    private BigDecimal progressPercent;

    @Schema(example = "2")
    private Integer completedSessions;

    @Schema(example = "10")
    private Integer totalSessions;

    @Schema(example = "IN_PROGRESS")
    private String status;
}


