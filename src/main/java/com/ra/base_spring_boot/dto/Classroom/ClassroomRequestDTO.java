package com.ra.base_spring_boot.dto.Classroom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ClassroomRequestDTO {

    @Schema(example = "Java Web K1")
    private String className;

    @Schema(example = "Lớp học nền tảng Java Web cho người mới bắt đầu")
    private String description;

    @Schema(example = "35")
    private Integer maxStudents;

    @Schema(example = "2025-01-05")
    private String startDate;

    @Schema(example = "2025-04-05")
    private String endDate;

    @Schema(example = "T2-4-6, 18:00-20:00")
    private String scheduleInfo;

    @Schema(example = "UPCOMING")
    private String status;
}

