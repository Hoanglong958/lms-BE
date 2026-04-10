package com.ra.base_spring_boot.repository.course;

import com.ra.base_spring_boot.model.RoadmapAssignment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IRoadmapAssignmentRepository extends JpaRepository<RoadmapAssignment, Long> {

    @EntityGraph(attributePaths = {"items", "items.session", "items.lesson"})
    Optional<RoadmapAssignment> findByClazz_IdAndCourse_Id(Long classId, Long courseId);

    boolean existsByClazz_IdAndCourse_Id(Long classId, Long courseId);

    void deleteByClazz_IdAndCourse_Id(Long classId, Long courseId);
}
