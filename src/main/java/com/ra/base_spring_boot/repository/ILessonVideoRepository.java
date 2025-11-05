package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.LessonVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ILessonVideoRepository extends JpaRepository<LessonVideo, Long> {
    List<LessonVideo> findByLesson_Id(Long lessonId);
}
