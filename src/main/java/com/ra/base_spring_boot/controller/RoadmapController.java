package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.Roadmap.RoadmapAssignRequest;
import com.ra.base_spring_boot.dto.Roadmap.RoadmapResponse;
import com.ra.base_spring_boot.services.course.IRoadmapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roadmaps")
@RequiredArgsConstructor
@Tag(name = "33 - Learning Roadmap", description = "Gán lộ trình học cho khóa học thuộc ca học đã có TKB")
public class RoadmapController {

    private final IRoadmapService roadmapService;

    @PostMapping("/assign")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Gán lộ trình cho khóa học thuộc ca học")
    public ResponseEntity<RoadmapResponse> assign(@RequestBody RoadmapAssignRequest req) {
        return ResponseEntity.ok(roadmapService.assign(req));
    }

    @PostMapping("/bulk-assign")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Gán hàng loạt lộ trình")
    public ResponseEntity<List<RoadmapResponse>> bulkAssign(@RequestBody List<RoadmapAssignRequest> requests) {
        return ResponseEntity.ok(roadmapService.assignBulk(requests));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TEACHER','ROLE_USER')")
    @Operation(summary = "Xem lộ trình theo classId & courseId")
    public ResponseEntity<RoadmapResponse> get(@RequestParam Long classId, @RequestParam Long courseId) {
        return ResponseEntity.ok(roadmapService.get(classId, courseId));
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Xóa lộ trình của classId & courseId")
    public ResponseEntity<Void> clear(@RequestParam Long classId, @RequestParam Long courseId) {
        roadmapService.clear(classId, courseId);
        return ResponseEntity.noContent().build();
    }
}
