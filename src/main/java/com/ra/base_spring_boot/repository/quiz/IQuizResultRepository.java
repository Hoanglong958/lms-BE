package com.ra.base_spring_boot.repository.quiz;

import com.ra.base_spring_boot.model.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IQuizResultRepository extends JpaRepository<QuizResult, Long> {

    List<QuizResult> findByUser_IdAndDeletedFalse(Long userId);

    List<QuizResult> findByQuiz_IdAndDeletedFalse(Long quizId);

    List<QuizResult> findAllByDeletedFalse();

    Optional<QuizResult> findByIdAndDeletedFalse(Long id);

    @Query("SELECT AVG(r.score) FROM QuizResult r")
    Double avgScoreAll();

    @Query("SELECT AVG(r.score) FROM QuizResult r WHERE r.submittedAt >= :since")
    Double avgScoreSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(r) FROM QuizResult r")
    long countAllAttempts();

    @Query("SELECT COUNT(r) FROM QuizResult r WHERE r.submittedAt >= :since")
    long countAttemptsSince(@Param("since") LocalDateTime since);

    // Pass/fail counts for a quiz
    @Query("SELECT COUNT(r) FROM QuizResult r WHERE r.quiz.id = :quizId AND r.score >= r.passScore")
    Long countPassByQuiz(@Param("quizId") Long quizId);

    @Query("SELECT COUNT(r) FROM QuizResult r WHERE r.quiz.id = :quizId")
    Long countAttemptByQuiz(@Param("quizId") Long quizId);


    @Query("SELECT AVG(r.score) FROM QuizResult r WHERE r.quiz.id = :quizId")
    Double avgScoreByQuiz(@Param("quizId") Long quizId);
}
