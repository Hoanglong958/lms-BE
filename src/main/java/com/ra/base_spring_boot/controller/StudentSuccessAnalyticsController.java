package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.Classroom.ClassroomResponseDTO;
import com.ra.base_spring_boot.dto.analytics.StudentSuccessAnalyticsResponse;
import com.ra.base_spring_boot.security.principle.MyUserDetails;
import com.ra.base_spring_boot.services.analytics.IStudentSuccessAnalyticsService;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics/student-success")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TEACHER')")
@RequiredArgsConstructor
public class StudentSuccessAnalyticsController {

    private final IStudentSuccessAnalyticsService analyticsService;

    @GetMapping("/class/{classId}")
    public ResponseEntity<StudentSuccessAnalyticsResponse> getClassAnalytics(
            @PathVariable Long classId,
            Authentication authentication) {
        var user = extractUser(authentication);
        return ResponseEntity.ok(analyticsService.getByClass(classId, user));
    }

    @GetMapping("/classes")
    public ResponseEntity<List<ClassroomResponseDTO>> getAccessibleClasses(Authentication authentication) {
        var user = extractUser(authentication);
        return ResponseEntity.ok(analyticsService.getAccessibleClasses(user));
    }

    private com.ra.base_spring_boot.model.User extractUser(Authentication authentication) {
        if (authentication == null) return null;
        var principal = authentication.getPrincipal();
        if (principal instanceof MyUserDetails) {
            return ((MyUserDetails) principal).getUser();
        }
        return null;
    }
}
