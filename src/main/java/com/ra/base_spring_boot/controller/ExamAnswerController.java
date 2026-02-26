package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.ExamAttempt.ExamAnswerDTO;
import com.ra.base_spring_boot.services.exam.IExamAnswerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exam-answers")
@RequiredArgsConstructor
@Tag(name = "12 - Exam Answers", description = "Xem đáp án chi tiết của lượt làm bài")
public class ExamAnswerController {

    private final IExamAnswerService examAnswerService;

    /**
     * ADMIN xem đáp án của bất kỳ attempt nào (phục vụ giám sát, hỗ trợ).
     */
    @GetMapping("/by-attempt/{attemptId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Danh sách đáp án theo attemptId (Admin)")
    public ResponseEntity<List<ExamAnswerDTO>> getByAttempt(@PathVariable Long attemptId) {
        return ResponseEntity.ok(examAnswerService.getByAttempt(attemptId, false));
    }

    /**
     * USER xem đáp án của chính lượt làm bài của mình.
     */
    @GetMapping("/my/{attemptId}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @Operation(summary = "Xem đáp án lượt làm của chính mình")
    public ResponseEntity<List<ExamAnswerDTO>> getMyAttemptAnswers(@PathVariable Long attemptId) {
        return ResponseEntity.ok(examAnswerService.getByAttempt(attemptId, true));
    }
}


