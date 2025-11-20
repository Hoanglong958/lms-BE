package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.LessonQuizzes.LessonQuizRequestDTO;
import com.ra.base_spring_boot.dto.LessonQuizzes.LessonQuizResponseDTO;

import java.util.List;

public interface ILessonQuizService {
    List<LessonQuizResponseDTO> getByLesson(Long lessonId);
    LessonQuizResponseDTO getById(Long id);
    LessonQuizResponseDTO create(LessonQuizRequestDTO dto);
    LessonQuizResponseDTO update(Long id, LessonQuizRequestDTO dto);
    void delete(Long id);
}
