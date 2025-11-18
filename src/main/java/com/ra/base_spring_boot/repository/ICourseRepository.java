package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICourseRepository extends JpaRepository<Course, Long> {
    @Query("SELECT c FROM Course c WHERE c.createdAt >= CURRENT_DATE - 30")
    List<Course> findNewCourses();
    Page<Course> findByTitleContainingIgnoreCaseOrInstructorNameContainingIgnoreCase(String title, String instructorName, Pageable pageable);
}