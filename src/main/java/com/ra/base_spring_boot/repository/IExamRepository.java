package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.Exam;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface IExamRepository extends JpaRepository<Exam, Long> {

    @Override
    @EntityGraph(attributePaths = "examQuestions")
    List<Exam> findAll();

    @Override
    @EntityGraph(attributePaths = "examQuestions")
    Optional<Exam> findById(Long id);
}
