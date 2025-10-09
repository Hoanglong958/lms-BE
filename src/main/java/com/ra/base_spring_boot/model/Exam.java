package com.ra.base_spring_boot.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Exams")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Exam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long exam_id;

    private String title;
    private String type;
    private Integer duration;
    private Double total_score;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;
}
