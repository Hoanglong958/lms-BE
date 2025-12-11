package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.dto.Quiz.CreateQuizQuestionRequest;
import com.ra.base_spring_boot.dto.Quiz.QuizQuestionDTO;
import com.ra.base_spring_boot.services.IQuizQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quiz-questions")
@RequiredArgsConstructor
@Tag(name = "13 - Quiz Questions", description = "Quản lý mối quan hệ quiz - question")
public class QuizQuestionController {

    private final IQuizQuestionService quizQuestionService;

    @GetMapping("/by-quiz/{quizId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    @Operation(summary = "Danh sách câu hỏi của một quiz")
    public ResponseEntity<List<QuizQuestionDTO>> getByQuiz(@PathVariable Long quizId) {
        return ResponseEntity.ok(quizQuestionService.getByQuiz(quizId));
        }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Thêm một câu hỏi vào quiz")
    public ResponseEntity<QuizQuestionDTO> create(@RequestBody CreateQuizQuestionRequest request) {
        return ResponseEntity.ok(quizQuestionService.create(request));
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Thêm nhiều câu hỏi vào quiz (bulk)")
    public ResponseEntity<List<QuizQuestionDTO>> createBulk(@RequestBody List<CreateQuizQuestionRequest> requests) {
        return ResponseEntity.ok(quizQuestionService.createBulk(requests));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Xóa một câu hỏi khỏi quiz")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        quizQuestionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
