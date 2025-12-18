package com.ra.base_spring_boot.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "schedule_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Khóa học
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    // Lớp học
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private Class clazz;

    // Ca học (period)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "period_id", nullable = false)
    private Period period;

    // Số thứ tự buổi trong khóa (1..totalSessions)
    @Column(nullable = false)
    private int sessionNumber;

    // Ngày của buổi (LocalDate) — dùng để hiển thị trên lịch
    @Column(nullable = false)
    private LocalDate date;

    // Thời gian bắt đầu / kết thúc (có thể map từ period + date)
    @Column
    private LocalDateTime startAt;

    @Column
    private LocalDateTime endAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    // trạng thái tuỳ dự án (SCHEDULED/CANCELLED/COMPLETED)
    @Column(nullable = false)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_course_id")
    private ClassCourse classCourse;

}
