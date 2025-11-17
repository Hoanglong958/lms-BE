package com.ra.base_spring_boot.dto.Classroom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ClassTeacherResponseDTO {

    @Schema(example = "2")
    private Long id;

    @Schema(example = "1")
    private Long classId;

    @Schema(example = "Java Web K1")
    private String className;

    @Schema(example = "3")
    private Long teacherId;

    @Schema(example = "Trần Thị C")
    private String teacherName;

    @Schema(example = "INSTRUCTOR")
    private String role;

    @Schema(example = "2025-01-02T09:00:00")
    private LocalDateTime assignedAt;

    @Schema(example = "Ghi chú giảng viên")
    private String note;
}

