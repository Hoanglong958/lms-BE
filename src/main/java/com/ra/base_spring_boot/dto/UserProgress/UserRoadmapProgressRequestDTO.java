package com.ra.base_spring_boot.dto.UserProgress;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UserRoadmapProgressRequestDTO {

    @Schema(example = "5")
    private Long userId;

    @Schema(example = "12")
    private Long roadmapId; // RoadmapAssignment.id

    @Schema(example = "IN_PROGRESS")
    private String status; // NOT_STARTED | IN_PROGRESS | COMPLETED

    @Schema(example = "101")
    private Long currentItemId; // RoadmapItem.id (session or lesson)
}
