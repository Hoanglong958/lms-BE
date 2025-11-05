package com.ra.base_spring_boot.dto.Course;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CourseResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String instructorName;
    private String level;
    private LocalDateTime createdAt;
}
