package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.Classroom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface IClassroomRepository extends JpaRepository<Classroom, Long> {
    Page<Classroom> findByClassNameContainingIgnoreCase(String keyword, Pageable pageable);
    long countByCreatedAtAfter(LocalDateTime sinceMonth);
}

