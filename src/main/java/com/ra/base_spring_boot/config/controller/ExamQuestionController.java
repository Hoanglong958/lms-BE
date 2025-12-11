package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.dto.Exam.CreateExamQuestionRequest;
import com.ra.base_spring_boot.dto.Exam.ExamQuestionDTO;
import com.ra.base_spring_boot.services.IExamQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exam-questions")
@RequiredArgsConstructor
@Tag(name = "11 - Exam Questions", description = "Quản lý mối quan hệ exam - question")
public class ExamQuestionController {

    private final IExamQuestionService examQuestionService;

    @GetMapping("/by-exam/{examId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    @Operation(summary = "Danh sách câu hỏi của một kỳ thi")
    public ResponseEntity<List<ExamQuestionDTO>> getByExam(@PathVariable Long examId) {
        return ResponseEntity.ok(examQuestionService.getByExam(examId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Thêm một câu hỏi vào kỳ thi")
    public ResponseEntity<ExamQuestionDTO> create(@RequestBody CreateExamQuestionRequest request) {
        return ResponseEntity.ok(examQuestionService.create(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Xóa một câu hỏi khỏi kỳ thi")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        examQuestionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}


