package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.ExamParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IExamParticipant extends JpaRepository<ExamParticipant, Long> {
    Optional<ExamParticipant> findByExamIdAndUserId(Long examId, Long userId);
}
