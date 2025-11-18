package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.config.dto.LessonExercise.LessonExerciseRequestDTO;
import com.ra.base_spring_boot.config.dto.LessonExercise.LessonExerciseResponseDTO;
import com.ra.base_spring_boot.model.Lesson;
import com.ra.base_spring_boot.model.LessonExercise;
import com.ra.base_spring_boot.repository.ILessonExerciseRepository;
import com.ra.base_spring_boot.repository.ILessonRepository;
import com.ra.base_spring_boot.services.ILessonExerciseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LessonExerciseServiceImpl implements ILessonExerciseService {

    private final ILessonExerciseRepository lessonExerciseRepository;
    private final ILessonRepository lessonRepository;

    /**
     * Tạo mới bài tập trong bài học
     */
    @Override
    public LessonExerciseResponseDTO create(LessonExerciseRequestDTO request) {
        Lesson lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        LessonExercise exercise = LessonExercise.builder()
                .lesson(lesson)
                .title(request.getTitle())
                .instructions(request.getInstructions())
                .requiredFields(request.getRequiredFields())
                .exampleCode(request.getExampleCode())
                .notes(request.getNotes())
                .build();

        lessonExerciseRepository.save(exercise);
        return toResponse(exercise);
    }

    /**
     * Lấy thông tin bài tập theo ID
     */
    @Override
    public LessonExerciseResponseDTO getById(Long id) {
        LessonExercise exercise = lessonExerciseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exercise not found"));
        return toResponse(exercise);
    }

    /**
     * Lấy danh sách bài tập theo bài học
     */
    @Override
    public List<LessonExerciseResponseDTO> getByLessonId(Long lessonId) {
        return lessonExerciseRepository.findByLesson_Id(lessonId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Cập nhật thông tin bài tập
     */
    @Override
    public LessonExerciseResponseDTO update(Long id, LessonExerciseRequestDTO request) {
        LessonExercise exercise = lessonExerciseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exercise not found"));

        exercise.setTitle(request.getTitle());
        exercise.setInstructions(request.getInstructions());
        exercise.setRequiredFields(request.getRequiredFields());
        exercise.setExampleCode(request.getExampleCode());
        exercise.setNotes(request.getNotes());

        lessonExerciseRepository.save(exercise);
        return toResponse(exercise);
    }

    /**
     * Xóa bài tập
     */
    @Override
    public void delete(Long id) {
        if (!lessonExerciseRepository.existsById(id)) {
            throw new RuntimeException("Exercise not found");
        }
        lessonExerciseRepository.deleteById(id);
    }

    /**
     * Chuyển đổi Entity sang ResponseDTO
     */
    private LessonExerciseResponseDTO toResponse(LessonExercise entity) {
        return LessonExerciseResponseDTO.builder()
                .exerciseId(entity.getId())
                .lessonId(entity.getLesson().getId())
                .title(entity.getTitle())
                .instructions(entity.getInstructions())
                .requiredFields(entity.getRequiredFields())
                .exampleCode(entity.getExampleCode())
                .notes(entity.getNotes())
                .build();
    }
}
