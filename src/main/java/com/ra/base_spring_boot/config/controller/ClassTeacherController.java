package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.dto.Classroom.ClassTeacherRequestDTO;
import com.ra.base_spring_boot.dto.Classroom.ClassTeacherResponseDTO;
import com.ra.base_spring_boot.services.IClassService;
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
@Tag(name = "14 - Class Teachers", description = "Quản lý giảng viên, trợ giảng trong lớp")
public class ClassTeacherController {

    private final IClassService classroomService;

    @PostMapping("/teachers")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TEACHER')")
    @Operation(summary = "Phân công giảng viên cho lớp")
    public ResponseEntity<ClassTeacherResponseDTO> assignTeacher(@RequestBody ClassTeacherRequestDTO dto) {
        return ResponseEntity.ok(classroomService.assignTeacher(dto));
    }

    @DeleteMapping("/{classId}/teachers/{teacherId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TEACHER')")
    @Operation(summary = "Gỡ giảng viên khỏi lớp")
    public ResponseEntity<Void> removeTeacher(@PathVariable Long classId, @PathVariable Long teacherId) {
        classroomService.removeTeacher(classId, teacherId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/teachers")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TEACHER','ROLE_USER')")
    @Operation(summary = "Danh sách giảng viên của lớp")
    public ResponseEntity<List<ClassTeacherResponseDTO>> listTeachers(@RequestParam Long classId) {
        return ResponseEntity.ok(classroomService.findTeachers(classId));
    }
}

