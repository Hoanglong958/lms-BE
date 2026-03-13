package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.QuizResult.QuizResultResponseDTO;
import com.ra.base_spring_boot.dto.QuizResult.QuizSubmissionRequestDTO;
import com.ra.base_spring_boot.exception.HttpForbiden;
import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.repository.user.IUserRepository;
import com.ra.base_spring_boot.services.quiz.IQuizResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quiz-results")
@RequiredArgsConstructor
@Tag(name = "18 - Quiz Results", description = "Quản lý kết quả làm bài Quiz")
public class QuizResultController {

    private final IQuizResultService quizResultService;
    private final IUserRepository userRepository;

    // ========== ADMIN: Xem tất cả kết quả ==========
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_USER')")
    @Operation(summary = "Lấy danh sách kết quả quiz", description = "Chỉ ADMIN được xem toàn bộ kết quả")
    @ApiResponse(responseCode = "200", description = "Thành công", content = @Content(schema = @Schema(implementation = QuizResultResponseDTO.class)))
    public ResponseEntity<List<QuizResultResponseDTO>> getAll() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdminOrTeacher = auth != null && auth.getAuthorities() != null && auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN") || a.equals("ROLE_TEACHER"));

        if (isAdminOrTeacher) {
            return ResponseEntity.ok(quizResultService.findAll());
        }

        String gmail = auth != null ? auth.getName() : null;
        if (gmail == null || gmail.isBlank()) {
            throw new HttpForbiden("Access denied");
        }
        User current = userRepository.findByGmailIgnoreCase(gmail)
                .orElseThrow(() -> new HttpForbiden("Access denied"));
        return ResponseEntity.ok(quizResultService.findByUser(current.getId()));
    }

    // ========== ADMIN: Xem chi tiết kết quả ==========
    @GetMapping("/detail")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_USER')")
    @Operation(summary = "Lấy chi tiết kết quả quiz", description = "Trả về kết quả quiz theo ID")
    public ResponseEntity<QuizResultResponseDTO> getById(@RequestParam Long id) {
        return ResponseEntity.ok(quizResultService.findById(id));
    }

    // ========== USER: Nộp bài quiz ==========
    @PostMapping("/submit")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "Nộp bài quiz", description = "Người dùng nộp bài quiz, hệ thống sẽ tự động tính điểm và lưu kết quả")
    @ApiResponse(responseCode = "200", description = "Nộp bài thành công", content = @Content(schema = @Schema(implementation = QuizResultResponseDTO.class)))
    public ResponseEntity<QuizResultResponseDTO> submitQuiz(
            @Valid @RequestBody QuizSubmissionRequestDTO request) {
        return ResponseEntity.ok(quizResultService.submitQuiz(request));
    }

    // ========== ADMIN: Xóa kết quả ==========
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
    @Operation(summary = "Xóa kết quả quiz", description = "ADMIN có thể xóa kết quả quiz theo ID")
    @ApiResponse(responseCode = "204", description = "Xóa thành công")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        quizResultService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
