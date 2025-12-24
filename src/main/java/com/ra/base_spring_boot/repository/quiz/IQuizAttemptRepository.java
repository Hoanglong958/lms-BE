package com.ra.base_spring_boot.repository.quiz;

import com.ra.base_spring_boot.model.quiz.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IQuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    @Query("select coalesce(max(qa.attemptNumber),0) from QuizAttempt qa where qa.quiz.id = :quizId and qa.user.id = :userId")
    int maxAttemptNumberByQuizAndUser(Long quizId, Long userId);

    List<QuizAttempt> findByUser_IdOrderByCreatedAtDesc(Long userId);

    List<QuizAttempt> findByQuiz_IdOrderByCreatedAtDesc(Long quizId);

    List<QuizAttempt> findByUser_IdAndQuiz_IdOrderByCreatedAtDesc(Long userId, Long quizId);

    List<QuizAttempt> findAllByOrderByCreatedAtDesc();
}
