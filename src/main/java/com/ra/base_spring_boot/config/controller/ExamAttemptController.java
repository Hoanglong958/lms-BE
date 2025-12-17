package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.dto.ExamAttempt.ExamAttemptResponseDTO;
import com.ra.base_spring_boot.dto.ExamAttempt.StartAttemptRequestDTO;
import com.ra.base_spring_boot.dto.ExamAttempt.SubmitAttemptRequestDTO;
import com.ra.base_spring_boot.services.IExamAttemptService;
import com.ra.base_spring_boot.exception.HttpNotFound;
import com.ra.base_spring_boot.exception.HttpForbiden;
import com.ra.base_spring_boot.repository.IUserRepository;
import com.ra.base_spring_boot.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exam-attempts")
@RequiredArgsConstructor
@Tag(name = "10 - Exam Attempts", description = "Quản lý lượt làm bài thi (exam attempts)")
public class ExamAttemptController {

    private final IExamAttemptService attemptService;
    private final IUserRepository userRepository;

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
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<ExamAttemptResponseDTO> getById(@RequestParam Long id) {
        try {
            ExamAttemptResponseDTO dto = attemptService.getById(id);
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth != null && auth.getAuthorities() != null && auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch("ROLE_ADMIN"::equals);
            if (!isAdmin) {
                String gmail = auth != null ? auth.getName() : null;
                if (gmail == null || gmail.isBlank()) {
                    throw new HttpForbiden("Access denied");
                }
                User current = userRepository.findByGmailIgnoreCase(gmail)
                        .orElseThrow(() -> new HttpForbiden("Access denied"));
                if (dto.getUserId() == null || !dto.getUserId().equals(current.getId())) {
                    throw new HttpForbiden("Access denied");
                }
            }
            return ResponseEntity.ok(dto);
        } catch (RuntimeException ex) {
            throw new HttpNotFound("Attempt not found");
        }
    }

    // ADMIN: Theo exam (qua query param examId)
    @GetMapping(params = {"examId", "!userId"})
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<ExamAttemptResponseDTO>> getByExam(@RequestParam Long examId) {
        return ResponseEntity.ok(attemptService.getByExam(examId));
    }

    // ADMIN hoặc USER: Theo user (qua query param userId) - USER chỉ xem được chính mình
    @GetMapping(params = {"userId", "!examId"})
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<List<ExamAttemptResponseDTO>> getByUser(@RequestParam Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities() != null && auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        Long effectiveUserId = userId;
        if (!isAdmin) {
            String gmail = auth != null ? auth.getName() : null;
            if (gmail == null || gmail.isBlank()) {
                return ResponseEntity.status(403).build();
            }
            User current = userRepository.findByGmailIgnoreCase(gmail)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng hiện tại"));
            effectiveUserId = current.getId();
        }
        return ResponseEntity.ok(attemptService.getByUser(effectiveUserId));
    }

    // ADMIN hoặc USER: Khi truyền cả examId và userId → trả về danh sách theo user và lọc theo examId
    // USER chỉ xem được chính mình, nếu khác sẽ tự động thay bằng user hiện tại
    @GetMapping(params = {"examId", "userId"})
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<List<ExamAttemptResponseDTO>> getByUserAndExam(@RequestParam Long examId,
                                                                         @RequestParam Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities() != null && auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        Long effectiveUserId = userId;
        if (!isAdmin) {
            String gmail = auth != null ? auth.getName() : null;
            if (gmail == null || gmail.isBlank()) {
                return ResponseEntity.status(403).build();
            }
            User current = userRepository.findByGmailIgnoreCase(gmail)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng hiện tại"));
            effectiveUserId = current.getId();
        }

        List<ExamAttemptResponseDTO> list = attemptService.getByUser(effectiveUserId)
                .stream()
                .filter(dto -> dto.getExamId() != null && dto.getExamId().equals(examId))
                .toList();
        return ResponseEntity.ok(list);
    }
}
