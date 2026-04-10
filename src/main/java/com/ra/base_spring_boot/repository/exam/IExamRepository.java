package com.ra.base_spring_boot.repository.exam;

import com.ra.base_spring_boot.model.Exam;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IExamRepository extends JpaRepository<Exam, Long> {

    @EntityGraph(attributePaths = "examQuestions")
    List<Exam> findAllByOrderByCreatedAtDesc();

    @Override
    @EntityGraph(attributePaths = "examQuestions")
    @NonNull
    Optional<Exam> findById(@NonNull Long id);

    @EntityGraph(attributePaths = "examQuestions")
    List<Exam> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime createdAt);

    @EntityGraph(attributePaths = "examQuestions")
    List<Exam> findByClassIdOrderByCreatedAtDesc(Long classId);

    @EntityGraph(attributePaths = "examQuestions")
    List<Exam> findByCourseIdOrderByCreatedAtDesc(Long courseId);

    @EntityGraph(attributePaths = "examQuestions")
    List<Exam> findByCourseIdInOrderByCreatedAtDesc(List<Long> courseIds);
}
