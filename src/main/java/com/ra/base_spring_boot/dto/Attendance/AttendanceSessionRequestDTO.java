package com.ra.base_spring_boot.dto.Attendance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AttendanceSessionRequestDTO {

    @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long classId;

    @Schema(example = "Buổi 1 - Giới thiệu")
    private String title;

    @Schema(example = "2025-01-15", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sessionDate;

    @Schema(example = "08:00:00")
    private String startTime;

    @Schema(example = "10:00:00")
    private String endTime;

    @Schema(example = "UPCOMING")
    private String status;
}

