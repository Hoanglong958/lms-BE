package com.ra.base_spring_boot.config.dto.Lesson;

import com.ra.base_spring_boot.model.constants.LessonType;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonRequestDTO {
    private String title;
    private LessonType type;
    private Integer orderIndex;
    private Long sessionId;
}
