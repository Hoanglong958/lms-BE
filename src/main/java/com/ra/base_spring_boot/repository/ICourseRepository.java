package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ICourseRepository extends JpaRepository<Course, Long> {
    Page<Course> findByTitleContainingIgnoreCaseOrInstructorNameContainingIgnoreCase(String title, String instructorName, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Course c")
    long countAll();

    @Query("SELECT COUNT(c) FROM Course c WHERE c.createdAt >= :since")
    long countSince(LocalDateTime since);

    @Query("SELECT COUNT(c) FROM Course c WHERE c.createdAt < :before")
    long countBefore(LocalDateTime before);

    @Query("SELECT c FROM Course c WHERE c.createdAt >= :since ORDER BY c.createdAt DESC")
    List<Course> findNewCoursesSince(LocalDateTime since);
}