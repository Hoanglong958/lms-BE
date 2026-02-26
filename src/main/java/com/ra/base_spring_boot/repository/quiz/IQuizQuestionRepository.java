package com.ra.base_spring_boot.repository.quiz;

import com.ra.base_spring_boot.model.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IQuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    List<QuizQuestion> findByQuiz_IdOrderByOrderIndexAscIdAsc(Long quizId);
    boolean existsByQuiz_IdAndQuestion_Id(Long quizId, Long questionId);
    long countByQuiz_Id(Long quizId);
}
