package com.ra.base_spring_boot.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseCreateReq {
    @NotBlank
    @Size(max = 255)
    private String title;

    private String description;

    @Size(max = 100)
    private String category;

    @NotNull
    private Integer teacherId; // references User.userId
}
