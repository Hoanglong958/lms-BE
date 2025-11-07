    package com.ra.base_spring_boot.controller;
    
    import com.ra.base_spring_boot.dto.LessonExercise.LessonExerciseRequestDTO;
    import com.ra.base_spring_boot.dto.LessonExercise.LessonExerciseResponseDTO;
    import com.ra.base_spring_boot.services.ILessonExerciseService;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.access.prepost.PreAuthorize;
    import org.springframework.web.bind.annotation.*;
    import io.swagger.v3.oas.annotations.tags.Tag;
    import io.swagger.v3.oas.annotations.Operation;
    import io.swagger.v3.oas.annotations.responses.ApiResponse;
    
    import java.util.List;
    
    @RestController
    @RequestMapping("/api/v1/lesson-exercises")
    @RequiredArgsConstructor
    @Tag(name = "Lesson Exercise", description = "Quản lý bài tập của từng bài học")
    public class LessonExerciseController {
    
        private final ILessonExerciseService lessonExerciseService;
    
        @GetMapping("/lesson/{lessonId}")
        @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
        @Operation(summary = "Danh sách bài tập theo bài học", description = "Lấy tất cả bài tập thuộc 1 bài học cụ thể")
        @ApiResponse(responseCode = "200", description = "Thành công")
        public ResponseEntity<List<LessonExerciseResponseDTO>> getByLesson(@PathVariable Long lessonId) {
            return ResponseEntity.ok(lessonExerciseService.getByLessonId(lessonId));
        }
    
        @GetMapping("/{id}")
        @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
        @Operation(summary = "Chi tiết bài tập", description = "Lấy thông tin chi tiết của 1 bài tập theo ID")
        @ApiResponse(responseCode = "200", description = "Thành công")
        public ResponseEntity<LessonExerciseResponseDTO> getById(@PathVariable Long id) {
            return ResponseEntity.ok(lessonExerciseService.getById(id));
        }
    
        @PostMapping
        @PreAuthorize("hasAuthority('ROLE_ADMIN')")
        @Operation(summary = "Tạo bài tập mới", description = "Chỉ ADMIN được phép thêm bài tập mới vào bài học")
        @ApiResponse(responseCode = "200", description = "Tạo thành công")
        public ResponseEntity<LessonExerciseResponseDTO> create(@RequestBody LessonExerciseRequestDTO dto) {
            return ResponseEntity.ok(lessonExerciseService.create(dto));
        }
    
        @PutMapping("/{id}")
        @PreAuthorize("hasAuthority('ROLE_ADMIN')")
        @Operation(summary = "Cập nhật bài tập", description = "Chỉ ADMIN được phép chỉnh sửa nội dung bài tập")
        @ApiResponse(responseCode = "200", description = "Cập nhật thành công")
        public ResponseEntity<LessonExerciseResponseDTO> update(
                @PathVariable Long id,
                @RequestBody LessonExerciseRequestDTO dto) {
            return ResponseEntity.ok(lessonExerciseService.update(id, dto));
        }
    
        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('ROLE_ADMIN')")
        @Operation(summary = "Xóa bài tập", description = "Chỉ ADMIN được phép xóa bài tập khỏi bài học")
        @ApiResponse(responseCode = "204", description = "Xóa thành công")
        public ResponseEntity<Void> delete(@PathVariable Long id) {
            lessonExerciseService.delete(id);
            return ResponseEntity.noContent().build();
        }
    }
