package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.ScheduleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.LocalDate;

public interface IScheduleItemRepository extends JpaRepository<ScheduleItem, Long> {
    List<ScheduleItem> findByCourseIdOrderBySessionNumber(Long courseId);
    List<ScheduleItem> findByCourse_IdAndClazz_IdOrderBySessionNumber(Long courseId, Long classId);
    boolean existsByCourse_IdAndClazz_Id(Long courseId, Long classId);
    void deleteByCourseId(Long courseId);

    @Query("SELECT s FROM ScheduleItem s WHERE s.course.id = :courseId AND s.clazz.id = :classId " +
            "AND (:status IS NULL OR LOWER(s.status) = LOWER(:status)) " +
            "AND (:from IS NULL OR s.date >= :from) " +
            "AND (:to IS NULL OR s.date <= :to) " +
            "AND (:periodId IS NULL OR s.period.id = :periodId) " +
            "ORDER BY s.sessionNumber")
    List<ScheduleItem> findByCourseClassWithFilters(
            @Param("courseId") Long courseId,
            @Param("classId") Long classId,
            @Param("status") String status,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("periodId") Long periodId
    );

    void deleteByCourse_IdAndClazz_Id(Long id, Long id1);

    // Added alias without underscores to avoid IDE resolution issues
    void deleteByCourseIdAndClazzId(Long courseId, Long classId);

}
