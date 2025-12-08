package com.ra.base_spring_boot.dto.Attendance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AttendanceRecordRequestDTO {

    @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long attendanceSessionId;

    @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long studentId;

    @Schema(example = "PRESENT")
    private String status;

    @Schema(example = "2025-01-15T08:05:00")
    private String checkinTime;

    @Schema(example = "Điểm danh đúng giờ")
    private String note;
}

