package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.UserProgress.*;

import java.util.List;

public interface IUserProgressService {

    // ===== Khóa học =====
    UserCourseProgressResponseDTO upsertCourseProgress(UserCourseProgressRequestDTO dto);

    List<UserCourseProgressResponseDTO> getCourseProgressByUser(Long userId);

    // ===== Session =====
    UserSessionProgressResponseDTO upsertSessionProgress(UserSessionProgressRequestDTO dto);

    List<UserSessionProgressResponseDTO> getSessionProgressByUserAndCourse(Long userId, Long courseId);

    // ===== Lesson =====
    UserLessonProgressResponseDTO upsertLessonProgress(UserLessonProgressRequestDTO dto);

    List<UserLessonProgressResponseDTO> getLessonProgressByUserAndCourse(Long userId, Long courseId);

    // ===== Roadmap (Assignment) =====
    UserRoadmapProgressResponseDTO upsertRoadmapProgress(UserRoadmapProgressRequestDTO dto);

    List<UserRoadmapProgressResponseDTO> getRoadmapProgressByUser(Long userId);

    UserRoadmapProgressResponseDTO getRoadmapProgressByUserAndRoadmap(Long userId, Long roadmapId);
}


