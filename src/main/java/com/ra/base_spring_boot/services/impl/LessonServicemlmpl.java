package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.Lesson.LessonRequestDTO;
import com.ra.base_spring_boot.dto.Lesson.LessonResponseDTO;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.Chapter;
import com.ra.base_spring_boot.model.Lesson;
import com.ra.base_spring_boot.repository.IChapterRepository;
import com.ra.base_spring_boot.repository.ILessonRepository;
import com.ra.base_spring_boot.services.ILessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class LessonServicemlmpl implements ILessonService {

    private final ILessonRepository lessonRepository;
    private final IChapterRepository chapterRepository;
    @Override
    public List<LessonResponseDTO> getByChapter(Long chapterId) {
        List<Lesson> lessons = lessonRepository.findByChapterId(chapterId);
        return lessons.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public LessonResponseDTO getById(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new HttpBadRequest("Lesson not found with id = " + id));
        return mapToResponse(lesson);
    }

    @Override
    public LessonResponseDTO create(LessonRequestDTO dto) {
        Chapter chapter = chapterRepository.findById(dto.getChapterId())
                .orElseThrow(() -> new HttpBadRequest("Chapter not found with id = " + dto.getChapterId()));

        Lesson lesson = new Lesson();
        lesson.setTitle(dto.getTitle());
        lesson.setContent(dto.getContent());
        lesson.setChapter(chapter);

        Lesson saved = lessonRepository.save(lesson);
        return mapToResponse(saved);
    }

    public void delete(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new HttpBadRequest("Lesson not found with id = " + id));
        lessonRepository.delete(lesson);
    }

    // 🔹 Helper: map Entity -> ResponseDTO
    private LessonResponseDTO mapToResponse(Lesson lesson) {
        return LessonResponseDTO.builder()
                .id(lesson.getLesson_id())
                .title(lesson.getTitle())
                .content(lesson.getContent())
                .chapterId(lesson.getChapter().getId())
                .chapterTitle(lesson.getChapter().getTitle())
                .build();
    }
}
