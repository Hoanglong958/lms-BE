package com.ra.base_spring_boot.model;

import com.ra.base_spring_boot.model.constants.ClassEnrollmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "class_students", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"class_id", "student_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private Classroom clazz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(name = "enrolled_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Builder.Default
    private LocalDateTime enrolledAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ClassEnrollmentStatus status = ClassEnrollmentStatus.ACTIVE;

    @Column(name = "final_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal finalScore = BigDecimal.ZERO;

    @Column(name = "attendance_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal attendanceRate = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String note;

    @PrePersist
    public void onCreate() {
        if (enrolledAt == null) {
            enrolledAt = LocalDateTime.now();
        }
        if (status == null) {
            status = ClassEnrollmentStatus.ACTIVE;
        }
        if (finalScore == null) {
            finalScore = BigDecimal.ZERO;
        }
        if (attendanceRate == null) {
            attendanceRate = BigDecimal.ZERO;
        }
    }
}

