package com.ra.base_spring_boot.model;

import com.ra.base_spring_boot.model.constants.ClassStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "classes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Class {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_name", nullable = false, length = 150)
    private String className;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "max_students")
    @Builder.Default
    private Integer maxStudents = 30;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "schedule_info", length = 255)
    private String scheduleInfo;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ======= Danh sách ScheduleItem =======
    @OneToMany(mappedBy = "clazz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScheduleItem> scheduleItems;

    // ================= Lifecycle callbacks =================
    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = createdAt;
        }
        if (maxStudents == null) {
            maxStudents = 30;
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ================= Tính status động =================
    public ClassStatus getCalculatedStatus() {
        LocalDate today = LocalDate.now();

        if (today.isBefore(startDate)) {
            return ClassStatus.UPCOMING;
        } else if (endDate == null || !today.isAfter(endDate)) {
            return ClassStatus.ONGOING;
        } else {
            return ClassStatus.COMPLETED;
        }
    }
}
