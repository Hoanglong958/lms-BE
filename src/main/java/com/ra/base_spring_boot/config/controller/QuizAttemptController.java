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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quiz-attempts")
@RequiredArgsConstructor
@Tag(name = "32 - Quiz Attempts", description = "Bắt đầu, nộp bài và quản lý lượt làm quiz")
public class QuizAttemptController {

    private final IQuizAttemptService attemptService;
    private final AttemptFileValidator fileValidator;
    private final AttemptAttachmentStorage attachmentStorage;

    @Operation(summary = "Danh sách tất cả lượt làm quiz")
    @GetMapping
    public ResponseEntity<List<QuizAttemptResponse>> findAll() {
        return ResponseEntity.ok(attemptService.findAll());
    }

    @Operation(summary = "Danh sách tất cả lượt làm quiz (phân trang)")
    @GetMapping("/paging")
    public ResponseEntity<Page<QuizAttemptResponse>> findAllPaging(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "createdAt,desc") String sort) {

        Sort sortObj;
        String[] sortParts = sort.split(",");
        if (sortParts.length == 2) {
            sortObj = Sort.by(Sort.Direction.fromString(sortParts[1]), sortParts[0]);
        } else {
            sortObj = Sort.by(Sort.Direction.DESC, "createdAt");
        }
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return ResponseEntity.ok(attemptService.findAll(pageable));
    }

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

    @Operation(summary = "Danh sách lượt làm của user trong 1 quiz cụ thể")
    @GetMapping("/by-user/{userId}/quiz/{quizId}")
    public ResponseEntity<List<QuizAttemptResponse>> byUserAndQuiz(@PathVariable Long userId,
            @PathVariable Long quizId) {
        return ResponseEntity.ok(attemptService.byUserAndQuiz(userId, quizId));
    }

    // ===== Upload đính kèm (tùy chọn sử dụng cho dạng tự luận) =====
    @Operation(summary = "Upload file đính kèm cho lượt làm", description = "Kiểm tra file và lưu local, trả URL công khai")
    @PostMapping(value = "/{attemptId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> upload(@PathVariable Long attemptId,
            @RequestParam("file") MultipartFile file) {
        fileValidator.validate(file);
        String url = attachmentStorage.storeAttemptFile(attemptId, file);
        return ResponseEntity.ok(url);
    }
}
