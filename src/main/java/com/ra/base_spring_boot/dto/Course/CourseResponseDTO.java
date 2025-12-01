package com.ra.base_spring_boot.dto.Course;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class CourseResponseDTO {
    @Schema(example = "1")
    private Long id;

    @Schema(example = "Spring Boot Fundamentals")
    private String title;

    @Schema(example = "Learn how to build REST APIs with Spring Boot 3")
    private String description;

    @Schema(example = "Nguyen Van A")
    private String instructorName;

    @Schema(example = "BEGINNER")
    private String level;

    @Schema(example = "2025-11-14T09:30:00")
    private LocalDateTime createdAt;

    @Schema(example = "2025-11-14T10:30:00")
    private LocalDateTime updatedAt;

    private int totalSessions;
    private int weeks;
    private LocalDate startDate;
}
