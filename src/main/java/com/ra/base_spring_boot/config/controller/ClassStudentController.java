package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.dto.Classroom.ClassStudentRequestDTO;
import com.ra.base_spring_boot.dto.Classroom.ClassStudentResponseDTO;
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
@Tag(name = "15 - Class Students", description = "Quản lý học viên trong lớp")
public class ClassStudentController {

    private final IClassroomService classroomService;

    @PostMapping("/students")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TEACHER')")
    @Operation(summary = "Thêm học viên vào lớp")
    public ResponseEntity<ClassStudentResponseDTO> enrollStudent(@RequestBody ClassStudentRequestDTO dto) {
        return ResponseEntity.ok(classroomService.enrollStudent(dto));
    }

    @DeleteMapping("/{classId}/students/{studentId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TEACHER')")
    @Operation(summary = "Xóa học viên khỏi lớp")
    public ResponseEntity<Void> removeStudent(@PathVariable Long classId, @PathVariable Long studentId) {
        classroomService.removeStudent(classId, studentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/students")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TEACHER','ROLE_USER')")
    @Operation(summary = "Danh sách học viên trong lớp")
    public ResponseEntity<List<ClassStudentResponseDTO>> listStudents(@RequestParam Long classId) {
        return ResponseEntity.ok(classroomService.findStudents(classId));
    }
}

