package com.ra.base_spring_boot.dto.Attendance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class AttendanceSessionResponseDTO {

    @Schema(example = "1")
    private Long attendanceSessionId;

    @Schema(example = "1")
    private Long classId;

    @Schema(example = "Java Web K1")
    private String className;

    @Schema(example = "Buổi 1 - Giới thiệu")
    private String title;

    @Schema(example = "2025-01-15")
    private LocalDate sessionDate;

    @Schema(example = "08:00:00")
    private LocalTime startTime;

    @Schema(example = "10:00:00")
    private LocalTime endTime;

    @Schema(example = "UPCOMING")
    private String status;

    @Schema(example = "2025-01-01T08:00:00")
    private LocalDateTime createdAt;

    @Schema(example = "2025-01-10T08:00:00")
    private LocalDateTime updatedAt;

    @Schema(example = "25")
    private Long totalRecords;
}

