package com.ra.base_spring_boot.dto.Classroom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ClassStudentResponseDTO {

    @Schema(example = "10")
    private Long id;

    @Schema(example = "1")
    private Long classId;

    @Schema(example = "Java Web K1")
    private String className;

    @Schema(example = "5")
    private Long studentId;

    @Schema(example = "Nguyễn Văn B")
    private String studentName;

    @Schema(example = "ACTIVE")
    private String status;

    @Schema(example = "8.5")
    private BigDecimal finalScore;

    @Schema(example = "92.50")
    private BigDecimal attendanceRate;

    @Schema(example = "2025-01-05T08:00:00")
    private LocalDateTime enrolledAt;

    @Schema(example = "Ghi chú khác")
    private String note;
}

