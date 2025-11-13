package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.ExamAttempt.ExamAttemptResponseDTO;
import com.ra.base_spring_boot.model.ExamAttempt;

import java.util.List;
import java.util.Map;

public interface IExamAttemptService {
    ExamAttemptResponseDTO startAttempt(Long examId, Long userId);
    ExamAttemptResponseDTO submitAttempt(Long attemptId);
    ExamAttemptResponseDTO gradeAttempt(Long attemptId);

    ExamAttemptResponseDTO getById(Long id);
    List<ExamAttemptResponseDTO> getAll();
    List<ExamAttemptResponseDTO> getByExam(Long examId);
    List<ExamAttemptResponseDTO> getByUser(Long userId);
    ExamAttempt createAttempt(Long examId, Long userId, String examRoomId);
    ExamAttempt submitExam(Long attemptId, Map<Long, String> answers);


}
