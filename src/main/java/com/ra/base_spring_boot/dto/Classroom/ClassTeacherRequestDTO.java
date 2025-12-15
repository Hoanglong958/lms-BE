package com.ra.base_spring_boot.dto.Classroom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ClassTeacherRequestDTO {

    @Schema(example = "1")
    private Long classId;

    @Schema(example = "3")
    private Long teacherId;

    @Schema(example = "Giảng viên chính buổi tối")
    private String note;
}

