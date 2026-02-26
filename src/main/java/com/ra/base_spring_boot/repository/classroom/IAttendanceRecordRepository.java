package com.ra.base_spring_boot.repository.classroom;

import com.ra.base_spring_boot.model.attendance.AttendanceRecord;
import com.ra.base_spring_boot.model.constants.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IAttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    List<AttendanceRecord> findBySession_Id(Long sessionId);
    Optional<AttendanceRecord> findBySession_IdAndStudent_Id(Long sessionId, Long studentId);

    // NEW: Find attendance records by class and date
    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.session.classroom.id = :classId AND ar.sessionDate = :date")
    List<AttendanceRecord> findByClassIdAndSessionDate(Long classId, LocalDate date);

    long countBySession_IdAndStatus(Long sessionId, AttendanceStatus status);

    @Query("select ar from AttendanceRecord ar where ar.session.id in (:sessionIds)")
    List<AttendanceRecord> findBySessionIds(List<Long> sessionIds);
}
