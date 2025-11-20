package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface IClassroomRepository extends JpaRepository<Classroom, Long> {
    long countByCreatedAtAfter(LocalDateTime sinceMonth);
}
