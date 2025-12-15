package com.ra.base_spring_boot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ra.base_spring_boot.model.constants.ExamStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exams")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    private Integer totalQuestions = 0;

    @Builder.Default
    private Integer maxScore = 100;

    @Builder.Default
    private Integer passingScore = 70;

    private Integer durationMinutes;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ExamStatus status = ExamStatus.UPCOMING;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    // ================== EXAM QUESTIONS ==================
    @Builder.Default
    @OneToMany(
            mappedBy = "exam",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @JsonIgnore   // <-- FIX QUAN TRỌNG (tránh lỗi JSON + Lazy + vòng lặp)
    private List<ExamQuestion> examQuestions = new ArrayList<>();

    // ================== EXAM ATTEMPTS ==================
    @Builder.Default
    @OneToMany(
            mappedBy = "exam",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @JsonIgnore
    private List<ExamAttempt> examAttempts = new ArrayList<>();

    // ================== CLEAR METHOD ==================
    public void clearAllRelations() {
        if (examQuestions != null) {
            examQuestions.clear();
        }
        if (examAttempts != null) {
            examAttempts.clear();
        }
    }
}
