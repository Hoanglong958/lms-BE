package com.ra.base_spring_boot.dto.Session;

import lombok.Data;

@Data
public class SessionRequestDTO {
    private String title;
    private Long courseId;
    private Integer orderIndex;
}
