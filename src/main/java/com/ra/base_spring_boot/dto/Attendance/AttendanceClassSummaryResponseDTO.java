package com.ra.base_spring_boot.dto.Attendance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AttendanceClassSummaryResponseDTO {
    @Schema(example = "5")
    private Long classId;

    @Schema(example = "10")
    private Long totalSessions;

    private List<SessionSummary> sessions;

    @Data
    @Builder
    public static class SessionSummary {
        @Schema(example = "2025-11-10")
        private String date;
        @Schema(example = "25")
        private Long present;
        @Schema(example = "3")
        private Long late;
        @Schema(example = "2")
        private Long absent;
        @Schema(example = "1")
        private Long excused;
    }
}
