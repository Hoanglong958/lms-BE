package com.ra.base_spring_boot.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_enrollments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEnrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "lesson_id")
    private Integer lessonId;

    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal progressPercentage = BigDecimal.ZERO;

    private LocalDateTime completedAt;
    @Builder.Default
    private Integer copyrightYear = LocalDateTime.now().getYear();
}