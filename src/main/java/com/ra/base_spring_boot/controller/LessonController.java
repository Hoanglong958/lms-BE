package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.Lesson.LessonRequestDTO;
import com.ra.base_spring_boot.dto.Lesson.LessonResponseDTO;
import com.ra.base_spring_boot.services.ILessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final ILessonService lessonService;

    // 🟢 Xem danh sách bài học trong chương
    @GetMapping("/chapter/{chapterId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_STUDENT')")
    public ResponseEntity<List<LessonResponseDTO>> getByChapter(@PathVariable Long chapterId) {
        return ResponseEntity.ok(lessonService.getByChapter(chapterId));
    }

    // 🟢 Xem chi tiết bài học
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_STUDENT')")
    public ResponseEntity<LessonResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(lessonService.getById(id));
    }

    // 🟡 Tạo mới bài học (ADMIN, TEACHER)
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<LessonResponseDTO> create(@RequestBody LessonRequestDTO dto) {
        return ResponseEntity.ok(lessonService.create(dto));
    }

    // 🔴 Xóa bài học (ADMIN, TEACHER)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        lessonService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
