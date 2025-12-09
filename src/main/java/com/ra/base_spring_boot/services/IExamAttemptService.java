package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.ExamAttempt.ExamAttemptResponseDTO;
import com.ra.base_spring_boot.model.ExamAttempt;

import java.util.List;
import java.util.Map;

public interface IExamAttemptService {
    // Khi user bắt đầu làm bài → tạo attempt
    ExamAttemptResponseDTO startAttempt(Long examId, Long userId);

    // Lưu câu trả lời
    ExamAttempt submitExam(Long attemptId, Map<Long, String> answers);

    // Kết thúc bài (chốt endTime)
    ExamAttemptResponseDTO submitAttempt(Long attemptId);

    // Chấm điểm
    ExamAttemptResponseDTO gradeAttempt(Long attemptId);

    // Get 1
    ExamAttemptResponseDTO getById(Long id);

    // Get all
    List<ExamAttemptResponseDTO> getAll();

    // Get theo exam
    List<ExamAttemptResponseDTO> getByExam(Long examId);

    // Get theo user
    List<ExamAttemptResponseDTO> getByUser(Long userId);
}
