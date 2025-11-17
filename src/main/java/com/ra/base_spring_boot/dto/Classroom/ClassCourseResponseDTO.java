package com.ra.base_spring_boot.dto.Classroom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ClassCourseResponseDTO {

    @Schema(example = "4")
    private Long id;

    @Schema(example = "1")
    private Long classId;

    @Schema(example = "Java Web K1")
    private String className;

    @Schema(example = "7")
    private Long courseId;

    @Schema(example = "Spring Boot – Tối T7")
    private String courseTitle;

    @Schema(example = "2025-01-03T08:00:00")
    private LocalDateTime assignedAt;

    @Schema(example = "Ghi chú khóa học")
    private String note;
}

