package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ILessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findBySession_Id(Long sessionId);
}
