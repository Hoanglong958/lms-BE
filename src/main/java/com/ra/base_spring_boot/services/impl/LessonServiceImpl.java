package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.Lesson.LessonRequestDTO;
import com.ra.base_spring_boot.dto.Lesson.LessonResponseDTO;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.Lesson;
import com.ra.base_spring_boot.model.Session;
import com.ra.base_spring_boot.repository.ILessonRepository;
import com.ra.base_spring_boot.repository.ISessionRepository;
import com.ra.base_spring_boot.services.ILessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements ILessonService {

    private final ILessonRepository lessonRepository;
    private final ISessionRepository sessionRepository;

    @Override
    public List<LessonResponseDTO> getBySession(Long sessionId) {
        List<Lesson> lessons = lessonRepository.findBySession_Id(sessionId);
        return lessons.stream().map(this::mapToResponse).toList();
    }

    @Override
    public LessonResponseDTO getById(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new HttpBadRequest("Lesson not found with id = " + id));
        return mapToResponse(lesson);
    }

    @Override
    public LessonResponseDTO create(LessonRequestDTO dto) {
        Session session = sessionRepository.findById(dto.getSessionId())
                .orElseThrow(() -> new HttpBadRequest("Session not found with id = " + dto.getSessionId()));

        Lesson lesson = Lesson.builder()
                .title(dto.getTitle())
                .type(dto.getType())
                .orderIndex(dto.getOrderIndex())
                .session(session)
                .build();

        Lesson saved = lessonRepository.save(lesson);
        return mapToResponse(saved);
    }

    @Override
    public LessonResponseDTO update(Long id, LessonRequestDTO dto) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new HttpBadRequest("Lesson not found with id = " + id));

        if (dto.getTitle() != null) lesson.setTitle(dto.getTitle());
        if (dto.getType() != null) lesson.setType(dto.getType());
        if (dto.getOrderIndex() != null) lesson.setOrderIndex(dto.getOrderIndex());

        Lesson updated = lessonRepository.save(lesson);
        return mapToResponse(updated);
    }

    @Override
    public void delete(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new HttpBadRequest("Lesson not found with id = " + id));
        lessonRepository.delete(lesson);
    }

    private LessonResponseDTO mapToResponse(Lesson lesson) {
        return LessonResponseDTO.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .type(lesson.getType())
                .orderIndex(lesson.getOrderIndex())
                .sessionId(lesson.getSession().getId())
                .sessionTitle(lesson.getSession().getTitle())
                .build();
    }
}
