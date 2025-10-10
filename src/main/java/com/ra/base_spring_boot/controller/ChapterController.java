package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.Chapter.ChapterRequestDTO;
import com.ra.base_spring_boot.dto.Chapter.ChapterResponseDTO;
import com.ra.base_spring_boot.services.IChapterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chapters")
@RequiredArgsConstructor
public class ChapterController {

    private final IChapterService chapterService;

    // 🟢 Xem danh sách chương theo course
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_STUDENT')")
    public ResponseEntity<List<ChapterResponseDTO>> getByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(chapterService.getByCourse(courseId));
    }

    // 🟢 Xem chi tiết chương
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_STUDENT')")
    public ResponseEntity<ChapterResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(chapterService.getById(id));
    }

    // 🟡 Tạo mới chương (ADMIN, TEACHER)
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<ChapterResponseDTO> create(@RequestBody ChapterRequestDTO dto) {
        return ResponseEntity.ok(chapterService.create(dto));
    }

    // 🔴 Xóa chương (ADMIN, TEACHER)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        chapterService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
