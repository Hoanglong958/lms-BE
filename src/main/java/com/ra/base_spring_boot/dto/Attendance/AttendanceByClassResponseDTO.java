package com.ra.base_spring_boot.dto.Attendance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AttendanceByClassResponseDTO {

    @Schema(example = "1")
    private Long classId;

    @Schema(example = "Java Web K1")
    private String className;

    @Schema(example = "1")
    private Long attendanceSessionId;

    @Schema(example = "Buổi 1 - Giới thiệu")
    private String sessionTitle;

    @Schema(example = "2025-01-15")
    private String sessionDate;

    @Schema(example = "UPCOMING")
    private String sessionStatus;

    private List<AttendanceRecordResponseDTO> records;

    @Schema(example = "25")
    private Long totalStudents;

    @Schema(example = "20")
    private Long presentCount;

    @Schema(example = "3")
    private Long absentCount;

    @Schema(example = "2")
    private Long lateCount;
}

