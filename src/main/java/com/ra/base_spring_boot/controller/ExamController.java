package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.Exam.AddQuestionsToExamDTO;
import com.ra.base_spring_boot.dto.Exam.ExamResponseDTO;
import com.ra.base_spring_boot.dto.Exam.ExamRequestDTO;
import com.ra.base_spring_boot.services.exam.IExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/v1/exams")
@RequiredArgsConstructor
@Tag(name = "09 - Exams", description = "Quản lý kỳ thi")
public class ExamController {

    private final IExamService examService;

    // ======= Tạo exam (ADMIN + TEACHER) =======
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
    @Operation(summary = "Tạo kỳ thi", description = "ADMIN hoặc TEACHER được phép tạo mới kỳ thi")
    @ApiResponse(responseCode = "200", description = "Tạo thành công")
    public ResponseEntity<ExamResponseDTO> createExam(@RequestBody ExamRequestDTO dto) {
        return ResponseEntity.ok(examService.createExam(dto));
    }

    // ======= Cập nhật exam (ADMIN + TEACHER) =======
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
    @Operation(summary = "Cập nhật kỳ thi", description = "ADMIN hoặc TEACHER được phép cập nhật kỳ thi")
    @ApiResponse(responseCode = "200", description = "Cập nhật thành công")
    public ResponseEntity<ExamResponseDTO> updateExam(
            @Parameter(description = "Mã kỳ thi") @PathVariable Long id,
            @RequestBody ExamRequestDTO dto) {
        return ResponseEntity.ok(examService.updateExam(id, dto));
    }

    // ======= Xóa exam (ADMIN + TEACHER) =======
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
    @Operation(summary = "Xóa kỳ thi", description = "ADMIN hoặc TEACHER được phép xóa kỳ thi")
    @ApiResponse(responseCode = "204", description = "Xóa thành công")
    public ResponseEntity<Void> deleteExam(@Parameter(description = "Mã kỳ thi") @PathVariable Long id) {
        examService.deleteExam(id);
        return ResponseEntity.noContent().build();
    }

    // ======= Lấy exam theo ID (ADMIN + TEACHER + USER) =======
    @GetMapping("/detail")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_USER')")
    @Operation(summary = "Lấy chi tiết kỳ thi", description = "Trả về thông tin kỳ thi theo ID")
    @ApiResponse(responseCode = "200", description = "Thành công")
    public ResponseEntity<ExamResponseDTO> getExam(@Parameter(description = "Mã kỳ thi") @RequestParam Long id) {
        return ResponseEntity.ok(examService.getExam(id));
    }

    // ======= Lấy tất cả exam (ADMIN + TEACHER + USER) =======
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_USER')")
    @Operation(summary = "Danh sách kỳ thi", description = "Trả về tất cả kỳ thi")
    @ApiResponse(responseCode = "200", description = "Thành công")
    public ResponseEntity<List<ExamResponseDTO>> getAllExams() {
        return ResponseEntity.ok(examService.getAllExams());
    }

    // ======= API thêm câu hỏi vào exam (ADMIN + TEACHER) =======
    @PostMapping("/{id}/questions")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
    @Operation(summary = "Thêm câu hỏi vào kỳ thi", description = "ADMIN hoặc TEACHER được thêm câu hỏi")
    public ResponseEntity<String> addQuestionsToExam(
            @Parameter(description = "Mã kỳ thi") @PathVariable Long id,
            @RequestBody AddQuestionsToExamDTO dto) {
        examService.addQuestionsToExam(id, dto.getQuestionIds());
        return ResponseEntity.ok("Questions added successfully");
    }
}
