package com.ra.base_spring_boot.services.course;

import com.ra.base_spring_boot.dto.Roadmap.RoadmapAssignRequest;
import com.ra.base_spring_boot.dto.Roadmap.RoadmapResponse;

import java.util.List;

public interface IRoadmapService {
    RoadmapResponse assign(RoadmapAssignRequest req);

    java.util.List<RoadmapResponse> assignBulk(java.util.List<RoadmapAssignRequest> requests);

    RoadmapResponse get(Long classId, Long courseId);

    void clear(Long classId, Long courseId);
}
