package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.Roadmap.RoadmapAssignRequest;
import com.ra.base_spring_boot.dto.Roadmap.RoadmapResponse;

import java.util.List;

public interface IRoadmapService {
    RoadmapResponse assign(RoadmapAssignRequest req);

    List<RoadmapResponse> assignBulk(List<RoadmapAssignRequest> requests);

    RoadmapResponse get(Long classId, Long courseId);

    void clear(Long classId, Long courseId);
}
