package com.ra.base_spring_boot.dto.Quiz;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class StartAttemptRequest {
    @Schema(example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long quizId;

    @Schema(example = "1001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;
}
