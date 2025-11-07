package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.LessonExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ILessonExerciseRepository extends JpaRepository<LessonExercise, Long> {
    List<LessonExercise> findByLesson_Id(Long lessonId);
}
