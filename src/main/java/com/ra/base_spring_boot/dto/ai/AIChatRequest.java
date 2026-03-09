package com.ra.base_spring_boot.dto.ai;

import lombok.Data;

@Data
public class AIChatRequest {
    /**
     * Loại yêu cầu: DEADLINE, EXAM, MATERIAL, QA
     */
    private String type;

    /**
     * Câu hỏi của người dùng
     */
    private String question;

    /**
     * ID khóa học (dùng cho MATERIAL)
     */
    private Long courseId;

    /**
     * ID bài học (dùng cho QA)
     */
    private Long lessonId;
}
