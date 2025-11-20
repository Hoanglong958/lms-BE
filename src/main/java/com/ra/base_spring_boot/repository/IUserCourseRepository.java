package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.UserCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface IUserCourseRepository extends JpaRepository<UserCourse, Long> {

    @Query("SELECT COUNT(uc) FROM UserCourse uc")
    long countTotal();

    @Query("SELECT COUNT(uc) FROM UserCourse uc WHERE uc.completed = true")
    long countCompleted();

    @Query("SELECT COUNT(uc) FROM UserCourse uc WHERE uc.enrolledAt >= :since")
    long countTotalSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(uc) FROM UserCourse uc WHERE uc.completed = true AND uc.completedAt >= :since")
    long countCompletedSince(@Param("since") LocalDateTime since);

    // counts per course
    long countByCourse_Id(Long courseId);
    long countByCourse_IdAndCompleted(Long courseId, boolean completed);

    @Query("SELECT COUNT(uc) FROM UserCourse uc WHERE uc.completed = true AND uc.completedAt < :before")
    long countCompletedBefore(@Param("before") LocalDateTime before);

    @Query("SELECT COUNT(uc) FROM UserCourse uc WHERE uc.enrolledAt < :before")
    long countTotalBefore(@Param("before") LocalDateTime before);
}
