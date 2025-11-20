package com.ra.base_spring_boot.model;

import com.ra.base_spring_boot.model.constants.LessonType;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "lessons")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    private String title;

    @Enumerated(EnumType.STRING)
    private LessonType type;

    private Integer orderIndex;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LessonQuiz> quizzes;
}
