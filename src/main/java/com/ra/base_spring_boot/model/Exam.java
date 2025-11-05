package com.ra.base_spring_boot.model;


import com.ra.base_spring_boot.model.constants.ExamStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
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


    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;


    private String title;


    @Column(columnDefinition = "TEXT")
    private String description;


    private Integer totalQuestions = 0;
    private Integer maxScore = 100;
    private Integer passingScore = 70;
    private Integer durationMinutes;


    private LocalDateTime startTime;
    private LocalDateTime endTime;


    @Enumerated(EnumType.STRING)
    private ExamStatus status = ExamStatus.UPCOMING;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExamQuestion> examQuestions;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExamAttempt> examAttempts;
}