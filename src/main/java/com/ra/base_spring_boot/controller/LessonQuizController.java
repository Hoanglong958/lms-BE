package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.LessonQuizzes.LessonQuizRequestDTO;
import com.ra.base_spring_boot.dto.LessonQuizzes.LessonQuizResponseDTO;
import com.ra.base_spring_boot.services.course.ILessonQuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lesson-quizzes")
@RequiredArgsConstructor
@Tag(name = "07 - Lesson Quizzes", description = "Quản lý quiz trong các bài học")
public class LessonQuizController {

    private final ILessonQuizService lessonQuizService;

    // ======= Danh sách quiz theo bài học =======
    @GetMapping(params = "lessonId")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @Operation(summary = "Danh sách quiz theo bài học", description = "Trả về danh sách quiz thuộc một bài học cụ thể")
    @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    public ResponseEntity<List<LessonQuizResponseDTO>> getByLesson(@RequestParam Long lessonId) {
        return ResponseEntity.ok(lessonQuizService.getByLesson(lessonId));
    }

    // ======= Chi tiết quiz =======
    @GetMapping("/detail")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @Operation(summary = "Chi tiết quiz", description = "Lấy thông tin chi tiết quiz theo ID")
    @ApiResponse(responseCode = "200", description = "Lấy thành công")
    public ResponseEntity<LessonQuizResponseDTO> getById(@RequestParam Long id) {
        return ResponseEntity.ok(lessonQuizService.getById(id));
    }

    // ======= Tạo quiz mới (ADMIN) =======
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Tạo quiz mới", description = "Chỉ ADMIN được phép tạo quiz mới")
    @ApiResponse(responseCode = "200", description = "Tạo quiz thành công")
    public ResponseEntity<LessonQuizResponseDTO> create(@RequestBody LessonQuizRequestDTO dto) {
        return ResponseEntity.ok(lessonQuizService.create(dto));
    }

    // ======= Cập nhật quiz (ADMIN) =======
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Cập nhật quiz", description = "Chỉ ADMIN được phép chỉnh sửa quiz")
    @ApiResponse(responseCode = "200", description = "Cập nhật thành công")
    public ResponseEntity<LessonQuizResponseDTO> update(
            @PathVariable Long id,
            @RequestBody LessonQuizRequestDTO dto
    ) {
        return ResponseEntity.ok(lessonQuizService.update(id, dto));
    }

    // ======= Xóa quiz (ADMIN) =======
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Xóa quiz", description = "Chỉ ADMIN được phép xóa quiz")
    @ApiResponse(responseCode = "204", description = "Xóa thành công")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        lessonQuizService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
