package com.ra.base_spring_boot.repository.common;

import com.ra.base_spring_boot.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface IAssignmentRepository extends JpaRepository<Assignment, Long> {
    @Query("SELECT COUNT(a) FROM Assignment a")
    long countAll();

    @Query("SELECT COUNT(a) FROM Assignment a WHERE a.createdAt >= :time")
    long countByCreatedAtAfter(@Param("time") LocalDateTime time);
}
