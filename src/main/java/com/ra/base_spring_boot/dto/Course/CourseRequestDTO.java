package com.ra.base_spring_boot.dto.Course;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Data
public class CourseRequestDTO {
    @Schema(example = "Spring Boot Fundamentals")
    private String title;

    @Schema(example = "Learn how to build REST APIs with Spring Boot 3")
    private String description;

    @Schema(example = "BEGINNER") // BEGINNER, INTERMEDIATE, ADVANCED
    private String level; // BEGINNER, INTERMEDIATE, ADVANCED

    @Schema(example = "10")
    private int totalSessions; // tổng số buổi học

    @Schema(example = "https://example.com/course-image.jpg")
    private String imageUrl;

    @Schema(example = "1000000.00")
    private java.math.BigDecimal tuitionFee;
}
