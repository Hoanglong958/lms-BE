package com.ra.base_spring_boot.repository.course;

import com.ra.base_spring_boot.model.LessonVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ILessonVideoRepository extends JpaRepository<LessonVideo, Long> {
    List<LessonVideo> findByLesson_Id(Long lessonId);

    @Query("select coalesce(max(v.orderIndex), 0) from LessonVideo v where v.lesson.id = :lessonId")
    Integer findMaxOrderIndexByLessonId(@Param("lessonId") Long lessonId);
}
