package com.ra.base_spring_boot.config.dto.Course;

import lombok.Data;

@Data
public class CourseRequestDTO {
    private String title;
    private String description;
    private String instructorName;
    private String level; // BEGINNER, INTERMEDIATE, ADVANCED
}
