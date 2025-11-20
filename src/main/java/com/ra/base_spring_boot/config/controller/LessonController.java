package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.dto.Lesson.LessonRequestDTO;
import com.ra.base_spring_boot.dto.Lesson.LessonResponseDTO;
import com.ra.base_spring_boot.services.ILessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/v1/lessons")
@RequiredArgsConstructor
@Tag(name = "04 - Lessons", description = "Quản lý bài học (lesson)")
public class LessonController {

    private final ILessonService lessonService;

    // ======= Lấy danh sách bài học theo session =======
    @GetMapping("/session/{sessionId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @Operation(summary = "Danh sách bài học theo session", description = "Trả về danh sách bài học thuộc 1 session")
    @ApiResponse(responseCode = "200", description = "Thành công")
    public ResponseEntity<List<LessonResponseDTO>> getBySession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(lessonService.getBySession(sessionId));
    }

    // ======= Lấy chi tiết bài học =======
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @Operation(summary = "Lấy chi tiết bài học", description = "Trả về thông tin bài học theo ID")
    @ApiResponse(responseCode = "200", description = "Thành công")
    public ResponseEntity<LessonResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(lessonService.getById(id));
    }

    // ======= Tạo bài học mới (chỉ ADMIN) =======
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Tạo bài học", description = "Chỉ ADMIN được phép tạo bài học mới")
    @ApiResponse(responseCode = "200", description = "Tạo thành công")
    public ResponseEntity<LessonResponseDTO> create(@RequestBody LessonRequestDTO dto) {
        return ResponseEntity.ok(lessonService.create(dto));
    }
    // ======= Cập nhật bài học (chỉ ADMIN) =======
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Cập nhật bài học", description = "Chỉ ADMIN được phép cập nhật thông tin bài học")
    @ApiResponse(responseCode = "200", description = "Cập nhật thành công")
    public ResponseEntity<LessonResponseDTO> update(
            @PathVariable Long id,
            @RequestBody LessonRequestDTO dto
    ) {
        return ResponseEntity.ok(lessonService.update(id, dto));
    }

    // ======= Xóa bài học (chỉ ADMIN) =======
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Xóa bài học", description = "Chỉ ADMIN được phép xóa bài học")
    @ApiResponse(responseCode = "204", description = "Xóa thành công")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        lessonService.delete(id);
        return ResponseEntity.noContent().build();
    }
}