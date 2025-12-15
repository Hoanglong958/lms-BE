package com.ra.base_spring_boot.dto.Classroom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ClassStudentRequestDTO {

    @Schema(example = "1")
    private Long classId;

    @Schema(example = "5")
    private Long studentId;

    private String gmail;

    @Schema(example = "ACTIVE")
    private String status;

    @Schema(example = "8.5")
    private BigDecimal finalScore;

    @Schema(example = "92.50")
    private BigDecimal attendanceRate;

    @Schema(example = "Học viên chuyển ca tối")
    private String note;
}

