package com.ra.base_spring_boot.dto.Course;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class CourseRequestDTO {
    @Schema(example = "Spring Boot Fundamentals")
    private String title;

    @Schema(example = "Learn how to build REST APIs with Spring Boot 3")
    private String description;

    @Schema(example = "Nguyen Van A")
    private String instructorName;

    @Schema(example = "BEGINNER") // BEGINNER, INTERMEDIATE, ADVANCED
    private String level; // BEGINNER, INTERMEDIATE, ADVANCED
}
