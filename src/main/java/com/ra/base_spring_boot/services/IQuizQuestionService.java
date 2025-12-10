package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.Quiz.CreateQuizQuestionRequest;
import com.ra.base_spring_boot.dto.Quiz.QuizQuestionDTO;

import java.util.List;

public interface IQuizQuestionService {
    List<QuizQuestionDTO> getByQuiz(Long quizId);
    QuizQuestionDTO create(CreateQuizQuestionRequest request);
    void delete(Long id);
}