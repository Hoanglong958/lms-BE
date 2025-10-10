package com.ra.base_spring_boot.dto.Lesson;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class LessonResponseDTO {
    private Long id;
    private String title;
    private String content;
    private Long chapterId;
    private String chapterTitle;
}
