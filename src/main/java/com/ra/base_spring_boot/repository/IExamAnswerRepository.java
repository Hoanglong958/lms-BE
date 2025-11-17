package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.ExamAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IExamAnswerRepository extends JpaRepository<ExamAnswer, Long> {
    long countByAttempt_IdAndIsCorrectTrue(Long attemptId);
    long countByAttempt_Id(Long attemptId);
    void deleteByAttempt_Id(Long attemptId);
}
