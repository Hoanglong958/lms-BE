package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.attendance.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {
    List<AttendanceSession> findByClassroom_IdOrderBySessionDateDesc(Long classId);
    List<AttendanceSession> findByClassroom_IdAndCourse_IdOrderBySessionDateDesc(Long classId, Long courseId);
    boolean existsByClassroom_IdAndSessionDate(Long classId, LocalDate date);
}
