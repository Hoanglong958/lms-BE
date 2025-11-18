package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.config.dto.Exam.ExamResponseDTO;
import com.ra.base_spring_boot.config.dto.Exam.ExamRequestDTO;
import java.util.List;

public interface IExamService {

    ExamResponseDTO createExam(ExamRequestDTO dto);

    ExamResponseDTO updateExam(Long examId, ExamRequestDTO dto);

    void deleteExam(Long examId);

    ExamResponseDTO getExam(Long examId);

    List<ExamResponseDTO> getAllExams();

    void addQuestionsToExam(Long examId, List<Long> questionIds);

}
