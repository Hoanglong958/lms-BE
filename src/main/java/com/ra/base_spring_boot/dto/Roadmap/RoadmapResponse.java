package com.ra.base_spring_boot.dto.Roadmap;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RoadmapResponse {
    private Long classId;
    private Long courseId;
    private List<RoadmapItemResponse> items;
}
