package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.LessonVideo.LessonVideoRequestDTO;
import com.ra.base_spring_boot.dto.LessonVideo.LessonVideoResponseDTO;
import com.ra.base_spring_boot.services.ILessonVideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lesson-videos")
@RequiredArgsConstructor
@Tag(name = "Lesson Video", description = "Quản lý nội dung video trong bài học")
public class LessonVideoController {

    private final ILessonVideoService lessonVideoService;

    // 🔹 Lấy danh sách video theo bài học
    @GetMapping("/lesson/{lessonId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @Operation(summary = "Danh sách video theo bài học", description = "Trả về danh sách video thuộc 1 bài học")
    @ApiResponse(responseCode = "200", description = "Thành công")
    public ResponseEntity<List<LessonVideoResponseDTO>> getByLesson(@PathVariable Long lessonId) {
        return ResponseEntity.ok(lessonVideoService.getByLesson(lessonId));
    }

    // 🔹 Lấy chi tiết video
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @Operation(summary = "Chi tiết video", description = "Lấy thông tin chi tiết của 1 video")
    @ApiResponse(responseCode = "200", description = "Thành công")
    public ResponseEntity<LessonVideoResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(lessonVideoService.getById(id));
    }

    // 🔹 Tạo mới video (ADMIN)
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Tạo video", description = "Chỉ ADMIN được phép tạo video")
    @ApiResponse(responseCode = "200", description = "Tạo thành công")
    public ResponseEntity<LessonVideoResponseDTO> create(@RequestBody LessonVideoRequestDTO dto) {
        return ResponseEntity.ok(lessonVideoService.create(dto));
    }

    // 🔹 Cập nhật video (ADMIN)
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Cập nhật video", description = "Chỉ ADMIN được phép chỉnh sửa video")
    @ApiResponse(responseCode = "200", description = "Cập nhật thành công")
    public ResponseEntity<LessonVideoResponseDTO> update(@PathVariable Long id,
                                                         @RequestBody LessonVideoRequestDTO dto) {
        return ResponseEntity.ok(lessonVideoService.update(id, dto));
    }

    // 🔹 Xóa video (ADMIN)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Xóa video", description = "Chỉ ADMIN được phép xóa video")
    @ApiResponse(responseCode = "204", description = "Xóa thành công")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        lessonVideoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
