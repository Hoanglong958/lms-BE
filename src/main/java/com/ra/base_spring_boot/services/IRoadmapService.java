package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.Roadmap.RoadmapAssignRequest;
import com.ra.base_spring_boot.dto.Roadmap.RoadmapResponse;

public interface IRoadmapService {
    RoadmapResponse assign(RoadmapAssignRequest req);
    RoadmapResponse get(Long classId, Long courseId);
    void clear(Long classId, Long courseId);
}
