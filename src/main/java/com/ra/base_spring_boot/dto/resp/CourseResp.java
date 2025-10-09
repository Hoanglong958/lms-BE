package com.ra.base_spring_boot.dto.resp;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CourseResp {
    private Integer id;
    private String title;
    private String description;
    private String category;
    private Integer teacherId;
    private String teacherUsername;
}
