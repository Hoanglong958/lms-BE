package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.LessonQuiz;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ILessonQuizRepository extends JpaRepository<LessonQuiz, Long> {
    List<LessonQuiz> findByLesson_Id(Long lessonId);
}
