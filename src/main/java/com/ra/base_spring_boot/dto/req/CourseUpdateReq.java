package com.ra.base_spring_boot.dto.req;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseUpdateReq {
    @Size(max = 255)
    private String title;

    private String description;

    @Size(max = 100)
    private String category;

    private Integer teacherId; // optional update
}
