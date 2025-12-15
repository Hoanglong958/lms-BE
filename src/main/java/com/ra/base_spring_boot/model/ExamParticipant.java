package com.ra.base_spring_boot.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Column(name = "join_time")
    private LocalDateTime joinTime;

    @Column(name = "started")
    @Builder.Default
    private Boolean started = false;

    @Column(name = "submit_time")
    private LocalDateTime submitTime;

    @Column(name = "submitted")
    @Builder.Default
    private Boolean submitted = false;
}
