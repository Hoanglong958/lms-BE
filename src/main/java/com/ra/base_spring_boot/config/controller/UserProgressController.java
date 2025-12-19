package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.dto.UserProgress.UserLessonProgressRequestDTO;
import com.ra.base_spring_boot.dto.UserProgress.UserLessonProgressResponseDTO;
import com.ra.base_spring_boot.dto.UserProgress.UserRoadmapProgressRequestDTO;
import com.ra.base_spring_boot.dto.UserProgress.UserRoadmapProgressResponseDTO;
import com.ra.base_spring_boot.services.IUserProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user-progress")
@RequiredArgsConstructor
@Tag(name = "34 - User Progress", description = "API tiến trình học theo bài học và trạng thái lộ trình")
public class UserProgressController {

    private final IUserProgressService userProgressService;

    @PostMapping("/lessons")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TEACHER','ROLE_USER')")
    @Operation(summary = "Lưu/cập nhật tiến trình bài học")
    public ResponseEntity<UserLessonProgressResponseDTO> upsertLessonProgress(
            @Valid @RequestBody UserLessonProgressRequestDTO dto
    ) {
        return ResponseEntity.ok(userProgressService.upsertLessonProgress(dto));
    }

    @GetMapping("/users/{userId}/courses/{courseId}/lessons")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TEACHER','ROLE_USER')")
    @Operation(summary = "Lấy tiến trình bài học theo user + course")
    public ResponseEntity<List<UserLessonProgressResponseDTO>> getLessonProgress(
            @PathVariable Long userId,
            @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(userProgressService.getLessonProgressByUserAndCourse(userId, courseId));
    }

    @PostMapping("/roadmaps")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TEACHER','ROLE_USER')")
    @Operation(summary = "Lưu/cập nhật trạng thái lộ trình học")
    public ResponseEntity<UserRoadmapProgressResponseDTO> upsertRoadmapProgress(
            @Valid @RequestBody UserRoadmapProgressRequestDTO dto
    ) {
        return ResponseEntity.ok(userProgressService.upsertRoadmapProgress(dto));
    }

    @GetMapping("/users/{userId}/roadmaps/{roadmapId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TEACHER','ROLE_USER')")
    @Operation(summary = "Lấy trạng thái lộ trình theo user + roadmapId")
    public ResponseEntity<UserRoadmapProgressResponseDTO> getRoadmapProgress(
            @PathVariable Long userId,
            @PathVariable Long roadmapId
    ) {
        return ResponseEntity.ok(userProgressService.getRoadmapProgressByUserAndRoadmap(userId, roadmapId));
    }
}

