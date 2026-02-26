package com.ra.base_spring_boot.repository.user;

import com.ra.base_spring_boot.model.UserLessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IUserLessonProgressRepository extends JpaRepository<UserLessonProgress, Long> {

    Optional<UserLessonProgress> findByUserIdAndLessonId(Long userId, Long lessonId);

    List<UserLessonProgress> findByUserIdAndSessionId(Long userId, Long sessionId);

    List<UserLessonProgress> findByUserIdAndCourseId(Long userId, Long courseId);
}
