package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.attendance.AttendanceRecord;
import com.ra.base_spring_boot.model.constants.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface IAttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    List<AttendanceRecord> findBySession_Id(Long sessionId);
    Optional<AttendanceRecord> findBySession_IdAndStudent_Id(Long sessionId, Long studentId);

    long countBySession_IdAndStatus(Long sessionId, AttendanceStatus status);

    @Query("select ar from AttendanceRecord ar where ar.session.id in (:sessionIds)")
    List<AttendanceRecord> findBySessionIds(List<Long> sessionIds);
}
