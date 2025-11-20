package com.ra.base_spring_boot.dto.LessonDocument;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonDocumentResponseDTO {
    private Long documentId;
    private Long lessonId;
    private String lessonTitle;
    private String title;
    private String content;
    private String imageUrl;
    private String videoUrl;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
