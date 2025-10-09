package com.ra.base_spring_boot.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Lessons")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lesson_id;

    private String title;
    private String content;
    private String video_url;
    private String material_file;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;
}
