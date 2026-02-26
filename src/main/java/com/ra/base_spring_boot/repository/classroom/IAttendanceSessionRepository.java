package com.ra.base_spring_boot.repository.classroom;

import com.ra.base_spring_boot.model.attendance.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface IAttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {
    @Query("SELECT s FROM AttendanceSession s JOIN FETCH s.classroom LEFT JOIN FETCH s.course WHERE s.classroom.id = :classId ORDER BY s.sessionDate DESC")
    List<AttendanceSession> findByClassroom_IdOrderBySessionDateDesc(Long classId);

    @Query("SELECT s FROM AttendanceSession s JOIN FETCH s.classroom LEFT JOIN FETCH s.course WHERE s.classroom.id = :classId AND s.course.id = :courseId ORDER BY s.sessionDate DESC")
    List<AttendanceSession> findByClassroom_IdAndCourse_IdOrderBySessionDateDesc(Long classId, Long courseId);

    boolean existsByClassroom_IdAndSessionDate(Long classId, LocalDate date);
}
