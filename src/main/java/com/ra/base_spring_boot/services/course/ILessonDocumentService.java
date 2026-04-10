package com.ra.base_spring_boot.services.course;

import com.ra.base_spring_boot.dto.LessonDocument.LessonDocumentRequestDTO;
import com.ra.base_spring_boot.dto.LessonDocument.LessonDocumentResponseDTO;

import java.util.List;

public interface ILessonDocumentService {
    List<LessonDocumentResponseDTO> getByLesson(Long lessonId);
    LessonDocumentResponseDTO getById(Long id);
    LessonDocumentResponseDTO create(LessonDocumentRequestDTO dto);
    LessonDocumentResponseDTO update(Long id, LessonDocumentRequestDTO dto);
    void delete(Long id);
}
