package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.Classroom.ClassStudentRequestDTO;
import com.ra.base_spring_boot.dto.Classroom.ClassStudentResponseDTO;
import com.ra.base_spring_boot.services.classroom.IClassService;
import com.ra.base_spring_boot.exception.HttpForbiden;
import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.repository.user.IUserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/classes")
@RequiredArgsConstructor
@Tag(name = "16 - Class Students", description = "Quản lý học viên trong lớp")
public class ClassStudentController {

    private final IClassService classroomService;
    private final IUserRepository userRepository;

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

    @GetMapping("/students/by-student")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TEACHER','ROLE_USER')")
    @Operation(summary = "Danh sách lớp học của học viên")
    public ResponseEntity<org.springframework.data.domain.Page<ClassStudentResponseDTO>> listClassesByStudent(
            @RequestParam Long studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdminOrTeacher = auth != null && auth.getAuthorities() != null && auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN") || a.equals("ROLE_TEACHER"));

        if (!isAdminOrTeacher) {
            String gmail = auth != null ? auth.getName() : null;
            if (gmail == null || gmail.isBlank()) {
                throw new HttpForbiden("Access denied");
            }
            User current = userRepository.findByGmailIgnoreCase(gmail)
                    .orElseThrow(() -> new HttpForbiden("Access denied"));
            if (!current.getId().equals(studentId)) {
                throw new HttpForbiden("Access denied");
            }
        }

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return ResponseEntity.ok(classroomService.findClassesByStudent(studentId, pageable));
    }
}

