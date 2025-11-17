package com.ra.base_spring_boot.dto.Classroom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ClassCourseRequestDTO {

    @Schema(example = "1")
    private Long classId;

    @Schema(example = "7")
    private Long courseId;

    @Schema(example = "Dạy kết hợp với khóa Spring Boot nâng cao")
    private String note;
}

