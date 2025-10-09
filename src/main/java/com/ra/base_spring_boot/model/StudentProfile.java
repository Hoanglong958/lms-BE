package com.ra.base_spring_boot.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Student_Profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfile {
    @Id
    private Long student_id;

    private Double progress;
    private Double attendance_rate;
    private String status;

    @OneToOne
    @JoinColumn(name = "student_id", referencedColumnName = "user_id")
    private User user;
}
