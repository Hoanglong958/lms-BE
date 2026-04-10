package com.ra.base_spring_boot.services.quiz;

import com.ra.base_spring_boot.dto.Quiz.CreateQuizQuestionRequest;
import com.ra.base_spring_boot.dto.Quiz.QuizQuestionDTO;

import java.util.List;

public interface IQuizQuestionService {
    List<QuizQuestionDTO> getByQuiz(Long quizId);

    QuizQuestionDTO create(CreateQuizQuestionRequest request);

    List<QuizQuestionDTO> createBulk(List<CreateQuizQuestionRequest> requests);

    void delete(Long id);
}
