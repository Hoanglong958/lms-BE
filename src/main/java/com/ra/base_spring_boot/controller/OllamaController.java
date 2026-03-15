package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.ai.AIChatRequest;
import com.ra.base_spring_boot.dto.ai.AIChatResponse;
import com.ra.base_spring_boot.services.ai.OllamaContextService;
import com.ra.base_spring_boot.services.ai.OllamaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller cho các tính năng AI hỗ trợ học tập (Ollama).
 * Endpoint: POST /api/v1/ai/chat
 *
 * Types:
 * - DEADLINE → Tra cứu deadline bài tập
 * - EXAM → Tra cứu lịch thi
 * - MATERIAL → Tra cứu tài liệu môn học
 * - QA → Hỏi đáp bài học
 */
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Slf4j
public class OllamaController {

    private final OllamaService ollamaService;
    private final OllamaContextService ollamaContextService;

    @PostMapping("/chat")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<AIChatResponse> chat(@RequestBody AIChatRequest request) {
        try {
            String type = request.getType() != null ? request.getType().toUpperCase() : "QA";
            String question = request.getQuestion();

            if (question == null || question.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(AIChatResponse.error("Vui lòng nhập câu hỏi."));
            }

            String context;
            String defaultQuestion;

            switch (type) {
                case "DEADLINE" -> {
                    context = ollamaContextService.buildDeadlineContext();
                    defaultQuestion = question.isBlank()
                            ? "Tôi có những bài tập nào cần nộp sắp tới?"
                            : question;
                }
                case "EXAM" -> {
                    context = ollamaContextService.buildExamContext();
                    defaultQuestion = question.isBlank()
                            ? "Lịch thi sắp tới của tôi như thế nào?"
                            : question;
                }
                case "MATERIAL" -> {
                    context = ollamaContextService.buildMaterialContext(request.getLessonId());
                    defaultQuestion = question.isBlank()
                            ? "Bài học này có những tài liệu nào?"
                            : question;
                }
                case "QA" -> {
                    context = ollamaContextService.buildQAContext(request.getLessonId());
                    defaultQuestion = question;
                }
                case "SCHEDULE" -> {
                    context = ollamaContextService.buildScheduleContext();
                    defaultQuestion = question.isBlank()
                            ? "Thời khóa biểu của tôi như thế nào?"
                            : question;
                }
                case "CLASS" -> {
                    context = ollamaContextService.buildClassContext();
                    defaultQuestion = question.isBlank()
                            ? "Tôi đang học những lớp nào?"
                            : question;
                }
                case "TEACHER" -> {
                    context = ollamaContextService.buildTeacherContext();
                    defaultQuestion = question.isBlank()
                            ? "Giảng viên đang dạy tôi là ai?"
                            : question;
                }
                default -> {
                    context = "";
                    defaultQuestion = question;
                }
            }

            log.info("[AI] Type: {} | Context Length: {}", type, context != null ? context.length() : 0);
            if (context != null && !context.isBlank()) {
                log.debug("[AI] Context: {}", context);
            }
            log.info("[AI] Final Question: {}", defaultQuestion);
            String answer = ollamaService.chat(context, defaultQuestion);

            return ResponseEntity.ok(AIChatResponse.ok(answer, type));

        } catch (Exception e) {
            log.error("[AI] Lỗi xử lý: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(AIChatResponse.error("Có lỗi xảy ra khi xử lý yêu cầu AI: " + e.getMessage()));
        }
    }

    /**
     * Health check – xem Ollama có đang chạy không.
     */
    @GetMapping("/health")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<AIChatResponse> health() {
        try {
            String pong = ollamaService.chat("",
                    "Chào! Hãy trả lời 'Tôi đang hoạt động bình thường!' bằng tiếng Việt.");
            return ResponseEntity.ok(AIChatResponse.ok(pong, "HEALTH"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(AIChatResponse.error("Ollama không phản hồi: " + e.getMessage()));
        }
    }
}
