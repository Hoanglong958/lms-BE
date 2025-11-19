package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.dto.Classroom.ClassCourseRequestDTO;
import com.ra.base_spring_boot.dto.Classroom.ClassCourseResponseDTO;
import com.ra.base_spring_boot.services.IClassroomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/classes")
@RequiredArgsConstructor
@Tag(name = "16 - Class Courses", description = "Quản lý khóa học gán vào lớp")
public class ClassCourseController {

    private final IClassroomService classroomService;

    @PostMapping("/courses")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TEACHER')")
    @Operation(summary = "Gán khóa học cho lớp")
    public ResponseEntity<ClassCourseResponseDTO> assignCourse(@RequestBody ClassCourseRequestDTO dto) {
        return ResponseEntity.ok(classroomService.assignCourse(dto));
    }

    @DeleteMapping("/{classId}/courses/{courseId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TEACHER')")
    @Operation(summary = "Gỡ khóa học khỏi lớp")
    public ResponseEntity<Void> removeCourse(@PathVariable Long classId, @PathVariable Long courseId) {
        classroomService.removeCourse(classId, courseId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{classId}/courses")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TEACHER','ROLE_USER')")
    @Operation(summary = "Danh sách khóa học của lớp")
    public ResponseEntity<List<ClassCourseResponseDTO>> listCourses(@PathVariable Long classId) {
        return ResponseEntity.ok(classroomService.findCourses(classId));
    }
}

