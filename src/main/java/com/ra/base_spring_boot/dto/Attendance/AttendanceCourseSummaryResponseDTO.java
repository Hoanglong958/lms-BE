package com.ra.base_spring_boot.dto.Attendance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AttendanceCourseSummaryResponseDTO {
    @Schema(example = "5")
    private Long classId;
    @Schema(example = "2")
    private Long courseId;
    @Schema(example = "8")
    private Long totalSessions;

    private List<Item> attendance;

    @Data
    @Builder
    public static class Item {
        @Schema(example = "12")
        private Long sessionId;
        @Schema(example = "2025-12-10")
        private String date;
        @Schema(example = "20")
        private Long present;
        @Schema(example = "5")
        private Long absent;
        @Schema(example = "3")
        private Long late;
        @Schema(example = "1")
        private Long excused;
    }
}
