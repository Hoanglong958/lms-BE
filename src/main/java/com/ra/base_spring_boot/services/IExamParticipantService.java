// ================= IExamParticipantService.java =================
package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.model.ExamParticipant;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IExamParticipantService {

    ExamParticipant joinExam(Long examId, Long userId, LocalDateTime joinTime);

    ExamParticipant submitExam(Long examId, Long userId, LocalDateTime submitTime);

    List<ExamParticipant> getParticipantsByExam(Long examId);

    ExamParticipant getParticipant(Long userId, Long examId);

    Optional<ExamParticipant> findByUserIdAndExamId(Long userId, Long examId);

}

