package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.dto.ExamAttempt.ExamAttemptResponseDTO;
import com.ra.base_spring_boot.dto.ExamAttempt.StartAttemptRequestDTO;
import com.ra.base_spring_boot.dto.ExamAttempt.SubmitAttemptRequestDTO;
import com.ra.base_spring_boot.services.IExamAttemptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exam-attempts")
@RequiredArgsConstructor
@Tag(name = "10 - Exam Attempts", description = "Quản lý lượt làm bài thi (exam attempts)")
public class ExamAttemptController {

    private final IExamAttemptService attemptService;

    // USER: Bắt đầu lượt làm
    @PostMapping("/start")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    @Operation(summary = "Bắt đầu lượt làm bài thi")
    public ResponseEntity<ExamAttemptResponseDTO> start(@RequestBody StartAttemptRequestDTO req) {
        return ResponseEntity.ok(attemptService.startAttempt(req.getExamId(), req.getUserId()));
    }

    // USER: Nộp bài
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    @Operation(
            summary = "Nộp bài thi",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = false,
                    description = "Danh sách câu trả lời thí sinh chọn",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SubmitAttemptRequestDTO.class),
                            examples = @ExampleObject(value = "{\n  \"answers\": [\n    { \"questionId\": 1, \"answer\": \"A\" },\n    { \"questionId\": 2, \"answer\": \"C\" },\n    { \"questionId\": 3, \"answer\": \"B\" }\n  ]\n}")
                    )
            )
    )
    public ResponseEntity<ExamAttemptResponseDTO> submit(
            @PathVariable Long id,
            @RequestBody(required = false) SubmitAttemptRequestDTO body
    ) {
        if (body != null && body.getAnswers() != null) {
            java.util.Map<Long, String> answersMap = new java.util.HashMap<>();
            for (SubmitAttemptRequestDTO.AnswerItem a : body.getAnswers()) {
                if (a != null && a.getQuestionId() != null && a.getAnswer() != null) {
                    answersMap.put(a.getQuestionId(), a.getAnswer());
                }
            }
            if (!answersMap.isEmpty()) {
                attemptService.submitExam(id, answersMap);
            }
        }
        return ResponseEntity.ok(attemptService.submitAttempt(id));
    }

    /**
     * ADMIN: Chấm điểm lượt làm
     */
    @PostMapping("/{id}/grade")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Chấm điểm lượt làm (Admin)")
    public ResponseEntity<ExamAttemptResponseDTO> grade(@PathVariable Long id) {
        return ResponseEntity.ok(attemptService.gradeAttempt(id));
    }
    // ADMIN: Danh sách toàn bộ
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<ExamAttemptResponseDTO>> getAll() {
        return ResponseEntity.ok(attemptService.getAll());
    }

    // ADMIN: Chi tiết theo id
    @GetMapping("/detail")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ExamAttemptResponseDTO> getById(@RequestParam Long id) {
        return ResponseEntity.ok(attemptService.getById(id));
    }

    // ADMIN: Theo exam
    @GetMapping(params = "examId")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<ExamAttemptResponseDTO>> getByExam(@RequestParam Long examId) {
        return ResponseEntity.ok(attemptService.getByExam(examId));
    }

    // ADMIN: Theo user
    @GetMapping(params = "userId")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<ExamAttemptResponseDTO>> getByUser(@RequestParam Long userId) {
        return ResponseEntity.ok(attemptService.getByUser(userId));
    }
}
