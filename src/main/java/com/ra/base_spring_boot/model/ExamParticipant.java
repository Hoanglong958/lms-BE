package com.ra.base_spring_boot.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "exam_participants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Long examRoomId;

    private LocalDateTime joinTime;

    @Builder.Default
    private Boolean started = false;

    @Builder.Default
    private Boolean submitted = false;
}