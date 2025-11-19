package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.Exam.ExamQuestionDTO;

import java.util.List;

public interface IExamQuestionService {
    List<ExamQuestionDTO> getByExam(Long examId);
    ExamQuestionDTO create(ExamQuestionDTO dto);
    void delete(Long id);
}


