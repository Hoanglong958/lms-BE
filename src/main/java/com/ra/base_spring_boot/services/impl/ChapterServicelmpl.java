package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.Chapter.ChapterRequestDTO;
import com.ra.base_spring_boot.dto.Chapter.ChapterResponseDTO;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.repository.IChapterRepository;
import com.ra.base_spring_boot.repository.ICourseRepository;
import com.ra.base_spring_boot.services.IChapterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.ra.base_spring_boot.model.Chapter;
import com.ra.base_spring_boot.model.Course;
import java.util.List;
@Service
@RequiredArgsConstructor
public class ChapterServicelmpl implements IChapterService {
    private final IChapterRepository chapterRepository;
    private final ICourseRepository courseRepository;

    @Override
    public List<ChapterResponseDTO> getByCourse(Long courseId) {
        List<Chapter> chapters = chapterRepository.findByCourse_Id(courseId);
        return chapters.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public ChapterResponseDTO getById(Long id) {
        Chapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new HttpBadRequest("Chapter not found with id = " + id));
        return mapToResponse(chapter);
    }
    @Override
    public ChapterResponseDTO create(ChapterRequestDTO dto) {
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new HttpBadRequest("Course not found with id = " + dto.getCourseId()));

        Chapter chapter = new Chapter();
        chapter.setTitle(dto.getTitle());
        chapter.setCourse(course);

        Chapter saved = chapterRepository.save(chapter);
        return mapToResponse(saved);
    }

    @Override
    public void delete(Long id) {
        Chapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new HttpBadRequest("Chapter not found with id = " + id));
        chapterRepository.delete(chapter);
    }
    // 🔹 Helper method: chuyển entity → DTO
    private ChapterResponseDTO mapToResponse(Chapter chapter) {
        return ChapterResponseDTO.builder()
                .id(chapter.getId())
                .title(chapter.getTitle())
                .courseId(chapter.getCourse().getId())
                .courseName(chapter.getCourse().getName())
                .build();
    }



}
