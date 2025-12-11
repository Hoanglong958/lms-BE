package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.Quiz.CreateQuizQuestionRequest;
import com.ra.base_spring_boot.dto.Quiz.QuizQuestionDTO;

import java.util.List;

public interface IQuizQuestionService {
    List<QuizQuestionDTO> getByQuiz(Long quizId);
    QuizQuestionDTO create(CreateQuizQuestionRequest request);
<<<<<<< HEAD
    List<QuizQuestionDTO> createBulk(List<CreateQuizQuestionRequest> requests);
    void delete(Long id);
}
=======
    void delete(Long id);
}
>>>>>>> 39439ec3d693d954bdd89aa9c5eacceb6c99bb97
