package com.ra.base_spring_boot.dto.Session;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SessionResponseDTO {
    private Long id;
    private String title;
    private Integer orderIndex;
    private Integer duration;
    private Long courseId;
    private String courseName;
}
