package com.ra.base_spring_boot.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private LessonQuiz quiz;  // Liên kết quiz

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;        // Người làm bài

    private Integer correctCount; // Số câu đúng
    private Integer totalCount;   // Tổng số câu
    private Integer score;        // Điểm
    private Boolean isPassed;     // Đậu / trượt

    @Builder.Default
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Builder.Default
    private boolean deleted = false;
    @Column(nullable = false)
    private Integer passScore;



}
