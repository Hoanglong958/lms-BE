package com.ra.base_spring_boot.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Company_Feedback")
public class CompanyFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Integer feedbackId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "company_id", referencedColumnName = "user_id", nullable = false)
    private com.ra.base_spring_boot.entity.User company; // company user

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id", referencedColumnName = "course_id", nullable = false)
    private Course course;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "rating")
    private Integer rating = 0; // 1-5
}
