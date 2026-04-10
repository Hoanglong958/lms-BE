package com.ra.base_spring_boot.dto.LessonDocument;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonDocumentRequestDTO {
    private Long lessonId; // FK tới lesson
    private String title;  // NOT NULL
    private String content; // LONGTEXT, nullable
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.ra.base_spring_boot.utils.ImageUrlDeserializer.class)
    private String imageUrl; // nullable

    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.ra.base_spring_boot.utils.ImageUrlDeserializer.class)
    private String videoUrl; // nullable

    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.ra.base_spring_boot.utils.ImageUrlDeserializer.class)
    private String pdfUrl; // nullable, public URL to uploaded PDF
    private Integer sortOrder; // optional, nếu null sẽ tự set max+1
}
