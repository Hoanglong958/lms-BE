package com.ra.base_spring_boot.config.dto.Session;

import lombok.Data;

@Data
public class SessionRequestDTO {
    private String title;
    private Integer duration; // thời lượng (phút)
    private Long courseId;
    private Integer orderIndex;
}
