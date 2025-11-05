package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.model.QuizResult;
import com.ra.base_spring_boot.services.IQuizResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quiz-results")
@RequiredArgsConstructor
@Tag(name = "QuizResult", description = "Quản lý kết quả làm bài Quiz")
public class QuizResultController {

    private final IQuizResultService quizResultService;

    // ========== ADMIN: Xem tất cả kết quả ==========
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Lấy danh sách kết quả quiz", description = "Chỉ ADMIN được xem toàn bộ kết quả")
    @ApiResponse(responseCode = "200", description = "Thành công",
            content = @Content(schema = @Schema(implementation = QuizResult.class)))
    public ResponseEntity<List<QuizResult>> getAll() {
        return ResponseEntity.ok(quizResultService.findAll());
    }

    // ========== ADMIN: Xem chi tiết kết quả ==========
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Lấy chi tiết kết quả quiz", description = "Trả về kết quả quiz theo ID")
    public ResponseEntity<QuizResult> getById(@PathVariable Long id) {
        QuizResult result = quizResultService.findById(id);
        return result != null ? ResponseEntity.ok(result) : ResponseEntity.notFound().build();
    }

    // ========== USER: Nộp bài quiz ==========
    @PostMapping("/submit")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "Nộp bài quiz", description = "Người dùng nộp bài quiz, hệ thống sẽ tự động tính điểm và lưu kết quả")
    @ApiResponse(responseCode = "200", description = "Nộp bài thành công",
            content = @Content(schema = @Schema(implementation = QuizResult.class)))
    public ResponseEntity<QuizResult> submitQuiz(
            @RequestParam Long quizId,
            @RequestParam Long userId,
            @RequestParam int correctCount,
            @RequestParam int totalCount
    ) {
        QuizResult result = quizResultService.submitQuiz(quizId, userId, correctCount, totalCount);
        return ResponseEntity.ok(result);
    }


    

    // ========== ADMIN: Xóa kết quả ==========
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Xóa kết quả quiz", description = "ADMIN có thể xóa kết quả quiz theo ID")
    @ApiResponse(responseCode = "204", description = "Xóa thành công")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        quizResultService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
