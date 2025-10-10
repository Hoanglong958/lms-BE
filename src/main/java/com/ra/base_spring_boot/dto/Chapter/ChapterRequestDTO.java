package com.ra.base_spring_boot.dto.Chapter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ChapterRequestDTO {
    private String title;
    private String description;
    private Long courseId;
}
