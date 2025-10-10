package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.Lesson.LessonRequestDTO;
import com.ra.base_spring_boot.dto.Lesson.LessonResponseDTO;
import java.util.*;

public interface ILessonService {
    List<LessonResponseDTO> getByChapter(Long chapterId);
    LessonResponseDTO getById(Long id);
    LessonResponseDTO create(LessonRequestDTO dto);
    void delete(Long id);
}
