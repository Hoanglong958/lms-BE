package com.ra.base_spring_boot.dto.Quiz;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SubmitAttemptRequest {
    @Schema(example = "85.50")
    private BigDecimal score;

    @Schema(example = "17")
    private Integer correctCount;

    @Schema(example = "20")
    private Integer totalCount;

    @Schema(example = "true")
    private Boolean passed;
}
