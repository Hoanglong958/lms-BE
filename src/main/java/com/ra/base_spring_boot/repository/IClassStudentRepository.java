package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.ClassStudent;
import com.ra.base_spring_boot.model.constants.ClassEnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface IClassStudentRepository extends JpaRepository<ClassStudent, Long> {

    boolean existsByClassroomIdAndStudentId(Long classroomId, Long studentId);

    Optional<ClassStudent> findByClassroomIdAndStudentId(Long classroomId, Long studentId);

    List<ClassStudent> findByClassroomId(Long classroomId);

    long countByClassroomId(Long classroomId);

    long countByClassroomIdAndStatus(Long classroomId, ClassEnrollmentStatus status);

    @Query("SELECT COALESCE(AVG(cs.finalScore),0) FROM ClassStudent cs WHERE cs.classroom.id = :classroomId")
    BigDecimal averageFinalScoreByClassroomId(Long classroomId);

    @Query("SELECT COALESCE(AVG(cs.attendanceRate),0) FROM ClassStudent cs WHERE cs.classroom.id = :classroomId")
    BigDecimal averageAttendanceRateByClassroomId(Long classroomId);
}


