package com.ra.base_spring_boot.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ExamResults")
public class ExamResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private Integer resultId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "exam_id", referencedColumnName = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", referencedColumnName = "user_id", nullable = false)
    private com.ra.base_spring_boot.entity.User student;

    @Column(name = "score", precision = 4, scale = 2)
    private BigDecimal score;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
}
