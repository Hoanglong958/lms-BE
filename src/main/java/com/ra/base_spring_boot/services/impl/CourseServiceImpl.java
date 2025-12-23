package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.Course.CourseRequestDTO;
import com.ra.base_spring_boot.dto.Course.CourseResponseDTO;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.Course;
import com.ra.base_spring_boot.model.constants.CourseLevel;
import com.ra.base_spring_boot.repository.ICourseRepository;
import com.ra.base_spring_boot.services.ICourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements ICourseService {

    private final ICourseRepository courseRepository;

    @Override
    public CourseResponseDTO create(CourseRequestDTO dto) {

        CourseLevel level = parseLevel(dto.getLevel());

        Course course = Course.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .level(level)
                .imageUrl(dto.getImageUrl())
                .totalSessions(dto.getTotalSessions())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        courseRepository.save(course);

        return toDto(course);
    }

    @Override
    public CourseResponseDTO update(Long id, CourseRequestDTO dto) {
        Course course = courseRepository.findById(Objects.requireNonNull(id, "id must not be null"))
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy khóa học với id = " + id));

        course.setTitle(dto.getTitle());
        course.setDescription(dto.getDescription());
        course.setLevel(parseLevel(dto.getLevel()));
        course.setImageUrl(dto.getImageUrl());
        course.setTotalSessions(dto.getTotalSessions());
        course.setUpdatedAt(LocalDateTime.now());

        courseRepository.save(java.util.Objects.requireNonNull(course, "course must not be null"));

        return toDto(course);
    }

    @Override
    public void delete(Long id) {
        Course course = courseRepository.findById(Objects.requireNonNull(id, "id must not be null"))
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy khóa học với id = " + id));
        courseRepository.delete(java.util.Objects.requireNonNull(course, "course must not be null"));
    }

    @Override
    public CourseResponseDTO findById(Long id) {
        Course course = courseRepository.findById(Objects.requireNonNull(id, "id must not be null"))
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy khóa học với id = " + id));

        return toDto(course);
    }

    @Override
    public List<CourseResponseDTO> findAll() {
        return courseRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public Page<CourseResponseDTO> findAll(Pageable pageable) {
        return courseRepository.findAll(pageable)
                .map(this::toDto);
    }

    @Override
    public Page<CourseResponseDTO> search(String keyword, Pageable pageable) {
        String kw = keyword == null ? "" : keyword.trim();

        return courseRepository
                .findByTitleContainingIgnoreCase(kw, pageable)
                .map(this::toDto);
    }

    // =========================== HELPER METHODS ===============================

    private CourseResponseDTO toDto(Course course) {
        return CourseResponseDTO.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .level(course.getLevel().name())
                .imageUrl(course.getImageUrl())
                .totalSessions(course.getTotalSessions())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }

    private CourseLevel parseLevel(String rawLevel) {
        if (rawLevel == null || rawLevel.trim().isEmpty()) {
            throw new HttpBadRequest("Level khóa học không được để trống (BEGINNER / INTERMEDIATE / ADVANCED)");
        }
        try {
            return CourseLevel.valueOf(rawLevel.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new HttpBadRequest("Level khóa học không hợp lệ. Giá trị hợp lệ: BEGINNER, INTERMEDIATE, ADVANCED");
        }
    }
}
