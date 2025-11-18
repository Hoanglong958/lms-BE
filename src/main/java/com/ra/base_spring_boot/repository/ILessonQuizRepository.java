package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.LessonQuiz;
import com.ra.base_spring_boot.model.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ILessonQuizRepository extends JpaRepository<LessonQuiz, Long> {
    List<LessonQuiz> findByLesson_Id(Long lessonId);

    @Query("SELECT q FROM QuizQuestion q WHERE q.createdAt >= CURRENT_DATE - 30")
    List<QuizQuestion> findRecentQuizzes();
}
