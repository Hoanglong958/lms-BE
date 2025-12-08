package com.ra.base_spring_boot.dto.Attendance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class AttendanceBulkRequestDTO {
    @Schema(example = "12", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long sessionId;

    private List<AttendanceRecordRequestDTO> records;
}
