package com.ra.base_spring_boot.repository.classroom;

import com.ra.base_spring_boot.model.Period;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;

@Repository
public interface IPeriodRepository extends JpaRepository<Period, Long> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    // Kiểm tra overlap thời gian, không cần dayOfWeek
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
            "FROM Period p " +
            "WHERE p.startTime < :endTime AND p.endTime > :startTime")
    boolean existsOverlap(@Param("startTime") LocalTime startTime,
                          @Param("endTime") LocalTime endTime);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
            "FROM Period p " +
            "WHERE p.id <> :id AND p.startTime < :endTime AND p.endTime > :startTime")
    boolean existsOverlapExceptId(@Param("startTime") LocalTime startTime,
                                  @Param("endTime") LocalTime endTime,
                                  @Param("id") Long id);

}
