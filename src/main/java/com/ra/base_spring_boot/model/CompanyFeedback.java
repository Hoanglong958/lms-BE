package com.ra.base_spring_boot.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Company_Feedback")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long feedback_id;

    private String content;
    private Double rating;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private CompanyProfile company;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;
}
