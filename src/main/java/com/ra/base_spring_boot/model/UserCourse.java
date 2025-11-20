package com.ra.base_spring_boot.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User đăng ký khóa học
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Khóa học được đăng ký
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    // % hoàn thành khóa học
    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal progressPercentage = BigDecimal.ZERO;

    // 0 = đang học, 1 = đã hoàn thành
    @Builder.Default
    private Boolean completed = false;

    // Điểm TB tất cả quiz trong khóa
    @Column(precision = 5, scale = 2)
    private BigDecimal averageScore;

    private LocalDateTime enrolledAt;

    private LocalDateTime completedAt;
}
