package com.ra.base_spring_boot.dto.LessonVideo;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonVideoRequestDTO {

    private Long lessonId; // ID của lesson chứa video

    private String title; // tiêu đề video

    private String videoUrl; // đường dẫn video (YouTube hoặc link nội bộ)

    private Integer durationSeconds; // thời lượng video (tính bằng giây)

    private String description; // mô tả chi tiết video
}
