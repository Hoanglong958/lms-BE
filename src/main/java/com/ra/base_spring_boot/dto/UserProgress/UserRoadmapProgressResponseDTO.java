package com.ra.base_spring_boot.dto.UserProgress;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserRoadmapProgressResponseDTO {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "5")
    private Long userId;

    @Schema(example = "12")
    private Long roadmapId;

    @Schema(example = "IN_PROGRESS")
    private String status;

    @Schema(example = "101")
    private Long currentItemId;

    @Schema(example = "3")
    private Integer completedItems;

    @Schema(example = "10")
    private Integer totalItems;

    @Schema(example = "30.00")
    private java.math.BigDecimal progressPercent;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime updatedAt;
}
