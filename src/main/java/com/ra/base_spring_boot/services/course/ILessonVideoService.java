package com.ra.base_spring_boot.services.course;

import com.ra.base_spring_boot.dto.LessonVideo.LessonVideoRequestDTO;
import com.ra.base_spring_boot.dto.LessonVideo.LessonVideoResponseDTO;

import java.util.List;

public interface ILessonVideoService {
    List<LessonVideoResponseDTO> getByLesson(Long lessonId);
    LessonVideoResponseDTO getById(Long id);
    LessonVideoResponseDTO create(LessonVideoRequestDTO dto);
    LessonVideoResponseDTO update(Long id, LessonVideoRequestDTO dto);
    void delete(Long id);
}

