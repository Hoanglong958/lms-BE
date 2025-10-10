package com.ra.base_spring_boot.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id") // ✅ khớp với DB
    private Long id;

    @Column(length = 255)
    private String category;

    @Column(length = 255)
    private String description;

    @Column(length = 255)
    private String title;

    @ManyToOne
    @JoinColumn(name = "teacher_id") // ✅ trùng với DB
    private User teacher;

    @Column(length = 255)
    private String name;
}
