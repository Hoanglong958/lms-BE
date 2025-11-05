package com.ra.base_spring_boot.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "lesson_videos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class LessonVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Liên kết bài học cha
    @ManyToOne
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    // Tiêu đề video
    @Column(nullable = false, length = 200)
    private String title;

    // Mô tả ngắn hoặc nội dung chi tiết
    @Column(columnDefinition = "TEXT")
    private String description;

    // URL video (link YouTube, file upload,...)
    @Column(name = "video_url", nullable = false)
    private String videoUrl;

    // Thời lượng video (tính bằng giây hoặc phút)
    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    // Thứ tự hiển thị trong bài học
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    // Ngày tạo và cập nhật (sử dụng Auditing)
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
