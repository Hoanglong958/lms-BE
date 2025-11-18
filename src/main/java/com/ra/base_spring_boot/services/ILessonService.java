package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.config.dto.Lesson.LessonRequestDTO;
import com.ra.base_spring_boot.config.dto.Lesson.LessonResponseDTO;

import java.util.List;

public interface ILessonService {
    List<LessonResponseDTO> getBySession(Long sessionId);
    LessonResponseDTO getById(Long id);
    LessonResponseDTO create(LessonRequestDTO dto);
    LessonResponseDTO update(Long id, LessonRequestDTO dto);
    void delete(Long id);
}
