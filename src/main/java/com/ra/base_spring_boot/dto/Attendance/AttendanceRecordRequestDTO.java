package com.ra.base_spring_boot.dto.Attendance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AttendanceRecordRequestDTO {

    @Schema(example = "1", required = true)
    private Long attendanceSessionId;

    @Schema(example = "1")
    private Long attendanceRecordId; // NEW: For updates

    public Long getAttendanceRecordId() {
        return attendanceRecordId;
    }

    @Schema(example = "1", required = true)
    private Long studentId;

    @Schema(example = "PRESENT")
    private String status;

    @Schema(example = "2025-01-15T08:05:00")
    private String checkinTime;

    @Schema(example = "Điểm danh đúng giờ")
    private String note;
}
