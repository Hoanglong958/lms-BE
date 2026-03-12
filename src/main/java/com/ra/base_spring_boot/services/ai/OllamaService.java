package com.ra.base_spring_boot.services.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Service gọi Ollama REST API để generate phản hồi AI bằng tiếng Việt.
 */
@Service
@Slf4j
public class OllamaService {

    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${ollama.model:qwen2.5:3b}")
    private String ollamaModel;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * System prompt đảm bảo AI luôn trả lời bằng tiếng Việt và đóng vai trợ lý học
     * tập.
     */
    private static final String SYSTEM_PROMPT = """
            Bạn là trợ lý học tập AI thông minh của hệ thống LMS (Learning Management System).
            Nhiệm vụ của bạn là hỗ trợ sinh viên trong việc:
            - Tra cứu deadline bài tập
            - Tra cứu lịch thi
            - Tìm kiếm tài liệu học tập
            - Giải đáp thắc mắc về bài học

            QUY TẮC QUAN TRỌNG:
            1. LUÔN LUÔN trả lời bằng tiếng Việt, kể cả khi được hỏi bằng ngôn ngữ khác.
            2. Trả lời ngắn gọn, dễ hiểu, thân thiện với sinh viên.
            3. Nếu không có thông tin cụ thể, hãy thành thật nói không biết và hướng dẫn sinh viên hỏi giáo viên.
            4. Không bịa đặt thông tin về deadline hoặc lịch thi.
            5. Dùng emoji phù hợp để câu trả lời sinh động hơn.
            """;

    /**
     * Gọi Ollama và lấy phản hồi.
     *
     * @param context  Dữ liệu ngữ cảnh từ DB (deadline, lịch thi, tài liệu...)
     * @param question Câu hỏi của người dùng
     * @return Câu trả lời từ AI
     */
    public String chat(String context, String question) {
        String fullPrompt = buildPrompt(context, question);

        try {
            Map<String, Object> requestBody = Map.of(
                    "model", ollamaModel,
                    "prompt", fullPrompt,
                    "stream", false,
                    "options", Map.of(
                            "temperature", 0.7,
                            "num_predict", 1024));

            String bodyJson = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ollamaBaseUrl + "/api/generate"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(120))
                    .POST(HttpRequest.BodyPublishers.ofString(bodyJson))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Map<?, ?> responseMap = objectMapper.readValue(response.body(), Map.class);
                Object responseText = responseMap.get("response");
                if (responseText != null) {
                    return responseText.toString().trim();
                }
            }

            log.error("Ollama trả về lỗi HTTP {}: {}", response.statusCode(), response.body());
            return "Xin lỗi, trợ lý AI hiện không thể xử lý yêu cầu của bạn. Vui lòng thử lại sau.";

        } catch (IOException | InterruptedException e) {
            log.error("Lỗi kết nối đến Ollama: {}", e.getMessage());
            Thread.currentThread().interrupt();
            return "Không thể kết nối đến dịch vụ AI. Hãy đảm bảo Ollama đang chạy tại " + ollamaBaseUrl;
        }
    }

    private String buildPrompt(String context, String question) {
        if (context == null || context.isBlank()) {
            return String.format("""
                    %s

                    CÂU HỎI CỦA SINH VIÊN: %s

                    TRẢ LỜI (bằng tiếng Việt):
                    """, SYSTEM_PROMPT, question);
        }

        return String.format("""
                %s

                THÔNG TIN HỆ THỐNG:
                %s

                CÂU HỎI CỦA SINH VIÊN: %s

                TRẢ LỜI (bằng tiếng Việt):
                """, SYSTEM_PROMPT, context, question);
    }
}
