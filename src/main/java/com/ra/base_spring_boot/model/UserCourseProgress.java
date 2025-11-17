package com.ra.base_spring_boot.model;

import com.ra.base_spring_boot.model.constants.UserCourseStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_courses", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "course_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCourseProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "enrolled_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Builder.Default
    private LocalDateTime enrolledAt = LocalDateTime.now();

    @Column(name = "progress_percent", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal progressPercent = BigDecimal.ZERO;

    @Column(name = "completed_sessions")
    @Builder.Default
    private Integer completedSessions = 0;

    @Column(name = "total_sessions")
    @Builder.Default
    private Integer totalSessions = 0;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserCourseStatus status = UserCourseStatus.ENROLLED;

    @PrePersist
    public void onCreate() {
        if (enrolledAt == null) {
            enrolledAt = LocalDateTime.now();
        }
        if (progressPercent == null) {
            progressPercent = BigDecimal.ZERO;
        }
        if (completedSessions == null) {
            completedSessions = 0;
        }
        if (totalSessions == null) {
            totalSessions = 0;
        }
        if (status == null) {
            status = UserCourseStatus.ENROLLED;
        }
    }
}


