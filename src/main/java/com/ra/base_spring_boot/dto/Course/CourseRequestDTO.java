package com.ra.base_spring_boot.dto.Course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CourseRequestDTO {
    private String name;
    private String title;
    private String description;
    private String category;

    public boolean getTeacherId() {
    }
}
