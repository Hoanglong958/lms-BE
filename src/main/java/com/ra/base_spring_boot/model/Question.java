package com.ra.base_spring_boot.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String category;

    @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
    private String questionText;

    // Lưu List<String> sang TEXT với converter
    @Convert(converter = ListToStringConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> options;

    @Column(name = "correct_answer", length = 255, nullable = false)
    private String correctAnswer;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "question")
    @Builder.Default
    private List<ExamQuestion> examQuestions = new ArrayList<>();


    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
