package com.ra.base_spring_boot.repository.course;

import com.ra.base_spring_boot.model.LessonQuiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ILessonQuizRepository extends JpaRepository<LessonQuiz, Long> {
    List<LessonQuiz> findByLesson_Id(Long lessonId);
    @Query("SELECT COUNT(lq) FROM LessonQuiz lq")
    long countAll();


    @Query("SELECT COUNT(lq) FROM LessonQuiz lq WHERE lq.createdAt >= :since")
    long countByCreatedAtAfter(@Param("since") LocalDateTime since);




    @Query("SELECT q FROM LessonQuiz q WHERE q.createdAt >= :since ORDER BY q.createdAt DESC")
    List<LessonQuiz> findRecentSince(@Param("since") LocalDateTime since);

}
