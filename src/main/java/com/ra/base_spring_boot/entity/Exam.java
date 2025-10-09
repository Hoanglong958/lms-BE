package com.ra.base_spring_boot.entity;

import com.ra.base_spring_boot.enums.ExamType;
import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Exams")
public class Exam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exam_id")
    private Integer examId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id", referencedColumnName = "course_id", nullable = false)
    private Course course;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20)
    private ExamType type;

    @Column(name = "duration")
    private Integer duration = 60; // minutes

    @Column(name = "total_score")
    private Integer totalScore = 100;
}
