package com.ra.base_spring_boot.dto.Classroom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassroomResponseDTO {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "Java Web K1")
    private String className;

    @Schema(example = "Lớp học nền tảng Java Web cho người mới bắt đầu")
    private String description;

    @Schema(example = "35")
    private Integer maxStudents;

    @Schema(example = "2025-01-05")
    private LocalDate startDate;

    @Schema(example = "2025-04-05")
    private LocalDate endDate;

    @Schema(example = "T2-4-6, 18:00-20:00")
    private String scheduleInfo;

    @Schema(example = "UPCOMING")
    private String status;  // ← vẫn giữ nhưng gán bằng getCalculatedStatus()

    @Schema(example = "2025-01-01T08:00:00")
    private LocalDateTime createdAt;

    @Schema(example = "2025-01-10T08:00:00")
    private LocalDateTime updatedAt;

    @Schema(example = "18")
    private Long totalStudents;

    @Schema(example = "2")
    private Long totalTeachers;

    @Schema(example = "3")
    private Long totalCourses;
}
