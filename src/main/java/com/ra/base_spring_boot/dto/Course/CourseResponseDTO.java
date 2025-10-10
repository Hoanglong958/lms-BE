package com.ra.base_spring_boot.dto.Course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponseDTO {
    private Long id;
    private String name;
    private String title;
    private String description;
    private String category;

    // thông tin teacher nếu cần hiển thị
    private Long teacherId;
    private String teacherName;
}
