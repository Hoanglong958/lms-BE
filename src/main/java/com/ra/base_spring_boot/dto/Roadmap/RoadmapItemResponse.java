package com.ra.base_spring_boot.dto.Roadmap;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoadmapItemResponse {
    private Long id;
    private Integer orderIndex;
    private Long sessionId;
    private String sessionTitle;
    private Long lessonId;
    private String lessonTitle;
}
