package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.dto.Quiz.QuizAttemptResponse;
import com.ra.base_spring_boot.dto.Quiz.StartAttemptRequest;
import com.ra.base_spring_boot.dto.Quiz.SubmitAttemptRequest;
import com.ra.base_spring_boot.services.IQuizAttemptService;
import com.ra.base_spring_boot.services.upload.AttemptAttachmentStorage;
import com.ra.base_spring_boot.services.validate.AttemptFileValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quiz-attempts")
@RequiredArgsConstructor
@Tag(name = "Quiz Attempts", description = "Bắt đầu, nộp bài và quản lý lượt làm quiz")
public class QuizAttemptController {

    private final IQuizAttemptService attemptService;
    private final AttemptFileValidator fileValidator;
    private final AttemptAttachmentStorage attachmentStorage;

    @Operation(summary = "Bắt đầu lượt làm quiz")
    @PostMapping("/start")
    public ResponseEntity<QuizAttemptResponse> start(@RequestBody StartAttemptRequest req) {
        return ResponseEntity.ok(attemptService.start(req));
    }

    @Operation(summary = "Nộp bài/ghi điểm lượt làm quiz")
    @PostMapping("/{attemptId}/submit")
    public ResponseEntity<QuizAttemptResponse> submit(@PathVariable Long attemptId,
                                                      @RequestBody SubmitAttemptRequest req) {
        return ResponseEntity.ok(attemptService.submit(attemptId, req));
    }

    @Operation(summary = "Chi tiết lượt làm")
    @GetMapping("/{attemptId}")
    public ResponseEntity<QuizAttemptResponse> get(@PathVariable Long attemptId) {
        return ResponseEntity.ok(attemptService.get(attemptId));
    }

    @Operation(summary = "Danh sách lượt làm của user")
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<List<QuizAttemptResponse>> byUser(@PathVariable Long userId) {
        return ResponseEntity.ok(attemptService.byUser(userId));
    }

    @Operation(summary = "Danh sách lượt làm của quiz")
    @GetMapping("/by-quiz/{quizId}")
    public ResponseEntity<List<QuizAttemptResponse>> byQuiz(@PathVariable Long quizId) {
        return ResponseEntity.ok(attemptService.byQuiz(quizId));
    }

    // ===== Upload đính kèm (tùy chọn sử dụng cho dạng tự luận) =====
    @Operation(summary = "Upload file đính kèm cho lượt làm", description = "Kiểm tra file và lưu local, trả URL công khai")
    @PostMapping(value = "/{attemptId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> upload(@PathVariable Long attemptId,
                                         @RequestPart("file") MultipartFile file) {
        fileValidator.validate(file);
        String url = attachmentStorage.storeAttemptFile(attemptId, file);
        return ResponseEntity.ok(url);
    }
}
