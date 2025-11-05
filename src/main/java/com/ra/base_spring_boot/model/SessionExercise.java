package com.ra.base_spring_boot.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "session_exercises")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionExercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ Đổi sang session thay vì lesson
    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

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
