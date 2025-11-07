package com.ra.base_spring_boot.model;


import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "lesson_exercises")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonExercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;


    private String title;


    @Column(columnDefinition = "TEXT")
    private String instructions;


    @Column(columnDefinition = "JSON")
    private String requiredFields;


    @Column(columnDefinition = "TEXT")
    private String exampleCode;


    @Column(columnDefinition = "TEXT")
    private String notes;
}