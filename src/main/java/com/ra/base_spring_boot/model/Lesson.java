package com.ra.base_spring_boot.model;

import com.ra.base_spring_boot.model.constants.LessonType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lessons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    private String title;

    @Enumerated(EnumType.STRING)
    private LessonType type;

    private Integer orderIndex;

    // ✅ Quiz
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LessonQuiz> quizzes = new ArrayList<>();

    // ✅ Video
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LessonVideo> videos = new ArrayList<>();

    // ✅ Document
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LessonDocument> documents = new ArrayList<>();
}
