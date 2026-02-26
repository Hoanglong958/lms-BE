package com.ra.base_spring_boot.repository.course;

import com.ra.base_spring_boot.model.LessonDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ILessonDocumentRepository extends JpaRepository<LessonDocument, Long> {
    List<LessonDocument> findByLesson_IdOrderBySortOrderAsc(Long lessonId);

    @Query("select coalesce(max(d.sortOrder), 0) from LessonDocument d where d.lesson.id = :lessonId")
    Integer findMaxSortOrderByLessonId(@Param("lessonId") Long lessonId);
}
