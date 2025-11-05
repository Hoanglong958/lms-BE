package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.LessonVideo.LessonVideoRequestDTO;
import com.ra.base_spring_boot.dto.LessonVideo.LessonVideoResponseDTO;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.Lesson;
import com.ra.base_spring_boot.model.LessonVideo;
import com.ra.base_spring_boot.repository.ILessonRepository;
import com.ra.base_spring_boot.repository.ILessonVideoRepository;
import com.ra.base_spring_boot.services.ILessonVideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonVideoServiceImpl implements ILessonVideoService {

    private final ILessonVideoRepository lessonVideoRepository;
    private final ILessonRepository lessonRepository;

    @Override
    public List<LessonVideoResponseDTO> getByLesson(Long lessonId) {
        List<LessonVideo> videos = lessonVideoRepository.findByLesson_Id(lessonId);
        return videos.stream().map(this::mapToResponse).toList();
    }

    @Override
    public LessonVideoResponseDTO getById(Long id) {
        LessonVideo video = lessonVideoRepository.findById(id)
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy video với id = " + id));
        return mapToResponse(video);
    }

    @Override
    public LessonVideoResponseDTO create(LessonVideoRequestDTO dto) {
        Lesson lesson = lessonRepository.findById(dto.getLessonId())
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy bài học với id = " + dto.getLessonId()));

        LessonVideo video = LessonVideo.builder()
                .lesson(lesson)
                .title(dto.getTitle())
                .videoUrl(dto.getVideoUrl())
                .description(dto.getDescription())
                .durationSeconds(dto.getDurationSeconds())
                .build();

        LessonVideo saved = lessonVideoRepository.save(video);
        return mapToResponse(saved);
    }

    @Override
    public LessonVideoResponseDTO update(Long id, LessonVideoRequestDTO dto) {
        LessonVideo video = lessonVideoRepository.findById(id)
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy video với id = " + id));

        if (dto.getTitle() != null) video.setTitle(dto.getTitle());
        if (dto.getVideoUrl() != null) video.setVideoUrl(dto.getVideoUrl());
        if (dto.getDurationSeconds() != null) video.setDurationSeconds(dto.getDurationSeconds());
        if (dto.getDescription() != null) video.setDescription(dto.getDescription());

        LessonVideo updated = lessonVideoRepository.save(video);
        return mapToResponse(updated);
    }

    @Override
    public void delete(Long id) {
        LessonVideo video = lessonVideoRepository.findById(id)
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy video với id = " + id));
        lessonVideoRepository.delete(video);
    }

    private LessonVideoResponseDTO mapToResponse(LessonVideo video) {
        return LessonVideoResponseDTO.builder()
                .videoId(video.getId())
                .title(video.getTitle())
                .videoUrl(video.getVideoUrl())
                .durationSeconds(video.getDurationSeconds())
                .description(video.getDescription())
                .lessonId(video.getLesson().getId())
                .lessonTitle(video.getLesson().getTitle())
                .createdAt(video.getCreatedAt())
                .updatedAt(video.getUpdatedAt())
                .build();
    }
}
