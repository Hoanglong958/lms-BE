package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.config.dto.LessonExercise.LessonExerciseRequestDTO;
import com.ra.base_spring_boot.config.dto.LessonExercise.LessonExerciseResponseDTO;

import java.util.List;

public interface ILessonExerciseService {
    LessonExerciseResponseDTO create(LessonExerciseRequestDTO request);
    LessonExerciseResponseDTO getById(Long id);
    List<LessonExerciseResponseDTO> getByLessonId(Long lessonId);
    LessonExerciseResponseDTO update(Long id, LessonExerciseRequestDTO request);
    void delete(Long id);
}
