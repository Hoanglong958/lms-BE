package com.ra.base_spring_boot.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Teacher_Profile")
public class TeacherProfile {
    @Id
    @Column(name = "teacher_id")
    private Integer teacherId; // FK to Users.user_id and also PK

    @OneToOne
    @JoinColumn(name = "teacher_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    private com.ra.base_spring_boot.entity.User teacher;

    @Column(name = "specialization", length = 255)
    private String specialization;

    @Column(name = "experience")
    private Integer experience = 0;
}
