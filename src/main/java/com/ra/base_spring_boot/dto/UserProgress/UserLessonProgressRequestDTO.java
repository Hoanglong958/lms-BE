package com.ra.base_spring_boot.dto.UserProgress;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserLessonProgressRequestDTO {

    @Schema(example = "5")
    private Long userId;

    @Schema(example = "11")
    private Long lessonId;

    @Schema(example = "7")
    private Long sessionId;

    @Schema(example = "3")
    private Long courseId;

    @Schema(example = "video")
    private String type;

    @Schema(example = "IN_PROGRESS")
    private String status;

    @Schema(example = "80.00")
    private BigDecimal progressPercent;
}


