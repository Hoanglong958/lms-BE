package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.LessonDocument.LessonDocumentRequestDTO;
import com.ra.base_spring_boot.dto.LessonDocument.LessonDocumentResponseDTO;
import com.ra.base_spring_boot.services.course.ILessonDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lesson-documents")
@RequiredArgsConstructor
@Tag(name = "21 - Lesson Documents", description = "Quản lý tài liệu/văn bản của bài học")
public class LessonDocumentController {

    private final ILessonDocumentService documentService;

    // Danh sách tài liệu theo lesson
    @GetMapping(params = "lessonId")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER', 'ROLE_TEACHER')")
    @Operation(summary = "Danh sách tài liệu theo bài học", description = "Trả về danh sách document của một lesson")
    @ApiResponse(responseCode = "200", description = "Thành công")
    public ResponseEntity<List<LessonDocumentResponseDTO>> getByLesson(@RequestParam Long lessonId) {
        return ResponseEntity.ok(documentService.getByLesson(lessonId));
    }

    // Chi tiết tài liệu
    @GetMapping("/detail")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER', 'ROLE_TEACHER')")
    @Operation(summary = "Chi tiết tài liệu", description = "Lấy tài liệu theo id")
    @ApiResponse(responseCode = "200", description = "Thành công")
    public ResponseEntity<LessonDocumentResponseDTO> getById(@RequestParam Long id) {
        return ResponseEntity.ok(documentService.getById(id));
    }

    // Tạo mới (ADMIN)
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
    @Operation(summary = "Tạo tài liệu", description = "Chỉ ADMIN được phép tạo")
    @ApiResponse(responseCode = "200", description = "Tạo thành công")
    public ResponseEntity<LessonDocumentResponseDTO> create(@RequestBody LessonDocumentRequestDTO dto) {
        return ResponseEntity.ok(documentService.create(dto));
    }

    // Cập nhật (ADMIN)
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
    @Operation(summary = "Cập nhật tài liệu", description = "Chỉ ADMIN được phép cập nhật")
    @ApiResponse(responseCode = "200", description = "Cập nhật thành công")
    public ResponseEntity<LessonDocumentResponseDTO> update(@PathVariable Long id,
                                                            @RequestBody LessonDocumentRequestDTO dto) {
        return ResponseEntity.ok(documentService.update(id, dto));
    }

    // Xóa (ADMIN)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
    @Operation(summary = "Xóa tài liệu", description = "Chỉ ADMIN được phép xóa")
    @ApiResponse(responseCode = "204", description = "Xóa thành công")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        documentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
