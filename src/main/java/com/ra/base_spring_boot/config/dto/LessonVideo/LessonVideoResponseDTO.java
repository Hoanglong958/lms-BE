package com.ra.base_spring_boot.config.dto.LessonVideo;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonVideoResponseDTO {

    private Long videoId;

    private String title;

    private String videoUrl;

    private Integer durationSeconds;

    private String description;

    private Long lessonId;

    private String lessonTitle;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
