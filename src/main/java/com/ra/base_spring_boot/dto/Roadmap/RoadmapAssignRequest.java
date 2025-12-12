package com.ra.base_spring_boot.dto.Roadmap;

import lombok.Data;

import java.util.List;

@Data
public class RoadmapAssignRequest {
    private Long classId;
    private Long courseId;
    private List<Long> sessionIds; // optional
    private List<Long> lessonIds;  // optional
    private List<Long> periodIds;  // optional: lọc theo ca học muốn gán
}
