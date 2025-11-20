package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.ClassStudent;
import com.ra.base_spring_boot.model.constants.ClassEnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface IClassStudentRepository extends JpaRepository<ClassStudent, Long> {

    boolean existsByClazzIdAndStudentId(Long classId, Long studentId);

    Optional<ClassStudent> findByClazzIdAndStudentId(Long classId, Long studentId);

    List<ClassStudent> findByClazzId(Long classId);

    long countByClazzId(Long classId);

    long countByClazzIdAndStatus(Long classId, ClassEnrollmentStatus status);

    @Query("SELECT COALESCE(AVG(cs.finalScore),0) FROM ClassStudent cs WHERE cs.clazz.id = :classId")
    BigDecimal averageFinalScoreByClassId(Long classId);

    @Query("SELECT COALESCE(AVG(cs.attendanceRate),0) FROM ClassStudent cs WHERE cs.clazz.id = :classId")
    BigDecimal averageAttendanceRateByClassId(Long classId);
}

