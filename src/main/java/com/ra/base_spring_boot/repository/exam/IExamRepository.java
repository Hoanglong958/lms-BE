package com.ra.base_spring_boot.repository.exam;

import com.ra.base_spring_boot.model.Exam;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IExamRepository extends JpaRepository<Exam, Long> {

    @Override
    @EntityGraph(attributePaths = "examQuestions")
    @NonNull
    List<Exam> findAll();

    @Override
    @EntityGraph(attributePaths = "examQuestions")
    @NonNull
    Optional<Exam> findById(@NonNull Long id);

    @EntityGraph(attributePaths = "examQuestions")
    List<Exam> findByCreatedAtAfter(LocalDateTime createdAt);

    @EntityGraph(attributePaths = "examQuestions")
    List<Exam> findByClassId(Long classId);

    @EntityGraph(attributePaths = "examQuestions")
    List<Exam> findByCourseId(Long courseId);

}
