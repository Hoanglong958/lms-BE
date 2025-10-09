package com.ra.base_spring_boot.entity;

import com.ra.base_spring_boot.enums.StudentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Student_Profile")
public class StudentProfile {
    @Id
    @Column(name = "student_id")
    private Integer studentId; // FK to Users.user_id and also PK

    @OneToOne
    @JoinColumn(name = "student_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    private com.ra.base_spring_boot.entity.User student;

    @Column(name = "progress", precision = 5, scale = 2)
    private BigDecimal progress = BigDecimal.ZERO;

    @Column(name = "attendance_rate", precision = 5, scale = 2)
    private BigDecimal attendanceRate = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private StudentStatus status = StudentStatus.active;
}
