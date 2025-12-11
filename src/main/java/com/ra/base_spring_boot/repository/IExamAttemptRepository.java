package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.ExamAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IExamAttemptRepository extends JpaRepository<ExamAttempt, Long> {
    List<ExamAttempt> findByExam_Id(Long examId);
    List<ExamAttempt> findByUser_Id(Long userId);
    Optional<ExamAttempt> findTopByExam_IdAndUser_IdOrderByAttemptNumberDesc(Long examId, Long userId);
    Optional<ExamAttempt> findTopByExam_IdAndUser_IdAndStatus(Long examId, Long userId, ExamAttempt.AttemptStatus status);

}
