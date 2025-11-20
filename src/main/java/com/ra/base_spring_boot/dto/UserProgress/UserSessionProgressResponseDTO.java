package com.ra.base_spring_boot.dto.UserProgress;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class UserSessionProgressResponseDTO {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "5")
    private Long userId;

    @Schema(example = "Nguyễn Văn A")
    private String userName;

    @Schema(example = "7")
    private Long sessionId;

    @Schema(example = "Session 1: Giới thiệu Spring Boot")
    private String sessionTitle;

    @Schema(example = "3")
    private Long courseId;

    @Schema(example = "Spring Boot Fundamentals")
    private String courseTitle;

    @Schema(example = "IN_PROGRESS")
    private String status;

    @Schema(example = "50.00")
    private BigDecimal progressPercent;

    @Schema(example = "2025-01-05T08:00:00")
    private LocalDateTime startedAt;

    @Schema(example = "2025-01-05T09:00:00")
    private LocalDateTime completedAt;
}


