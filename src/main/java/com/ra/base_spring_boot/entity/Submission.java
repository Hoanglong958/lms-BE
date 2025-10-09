package com.ra.base_spring_boot.entity;

import com.ra.base_spring_boot.enums.SubmissionStatus;
import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Submissions")
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_id")
    private Integer submissionId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "assignment_id", referencedColumnName = "assignment_id", nullable = false)
    private Assignment assignment;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", referencedColumnName = "user_id", nullable = false)
    private com.ra.base_spring_boot.entity.User student;

    @Column(name = "github_link", length = 500)
    private String githubLink;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private SubmissionStatus status = SubmissionStatus.pending;

    @Column(name = "grade", precision = 4, scale = 2)
    private java.math.BigDecimal grade;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;
}

