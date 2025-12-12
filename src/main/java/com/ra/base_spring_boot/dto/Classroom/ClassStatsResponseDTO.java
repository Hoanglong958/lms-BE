package com.ra.base_spring_boot.dto.Classroom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ClassStatsResponseDTO {

    @Schema(example = "1")
    private Long classId;

    @Schema(example = "Java Web K1")
    private String className;

    @Schema(example = "25")
    private Long totalStudents;

    @Schema(example = "20")
    private Long activeStudents;

    @Schema(example = "3")
    private Long completedStudents;

    @Schema(example = "2")
    private Long droppedStudents;

    @Schema(example = "85.50")
    private BigDecimal averageFinalScore;

    @Schema(example = "92.30")
    private BigDecimal averageAttendanceRate;

    @Schema(example = "2")
    private Long totalTeachers;

    @Schema(example = "1")
    private Long instructors;

    @Schema(example = "1")
    private Long assistants;

    @Schema(example = "3")
    private Long totalCourses;
}


