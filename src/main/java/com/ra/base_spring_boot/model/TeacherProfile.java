package com.ra.base_spring_boot.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Teacher_Profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherProfile {
    @Id
    private Long teacher_id;

    private String specialization;
    private String experience;

    @OneToOne
    @JoinColumn(name = "teacher_id", referencedColumnName = "user_id")
    private User user;
}
