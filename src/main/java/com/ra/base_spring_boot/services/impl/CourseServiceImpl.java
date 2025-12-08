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
                .instructorName(dto.getInstructorName())
                .level(level)
                .totalSessions(dto.getTotalSessions())
                .startDate(dto.getStartDate())
                .createdAt(LocalDateTime.now())
                .build();

        course.setUpdatedAt(course.getCreatedAt());
        courseRepository.save(course);

        return toDto(course);
    }

    @Override
    public CourseResponseDTO update(Long id, CourseRequestDTO dto) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy khóa học với id = " + id));

        course.setTitle(dto.getTitle());
        course.setDescription(dto.getDescription());
        course.setInstructorName(dto.getInstructorName());
        course.setLevel(parseLevel(dto.getLevel()));
        course.setTotalSessions(dto.getTotalSessions());
        course.setStartDate(dto.getStartDate());
        course.setUpdatedAt(LocalDateTime.now());

        courseRepository.save(course);

        return toDto(course);
    }

    @Override
    public void delete(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy khóa học với id = " + id));
        courseRepository.delete(course);
    }

    @Override
    public CourseResponseDTO findById(Long id) {
        Course course = courseRepository.findById(id)
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
        return courseRepository.findAll(pageable).map(this::toDto);
    }

    @Override
    public Page<CourseResponseDTO> search(String keyword, Pageable pageable) {
        String kw = keyword == null ? "" : keyword.trim();
        return courseRepository
                .findByTitleContainingIgnoreCaseOrInstructorNameContainingIgnoreCase(kw, kw, pageable)
                .map(this::toDto);
    }

    private CourseResponseDTO toDto(Course course) {
        return CourseResponseDTO.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .instructorName(course.getInstructorName())
                .level(course.getLevel().name())
                .totalSessions(course.getTotalSessions())
                .startDate(course.getStartDate())
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
