package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.config.dto.Course.CourseRequestDTO;
import com.ra.base_spring_boot.config.dto.Course.CourseResponseDTO;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.Course;
import com.ra.base_spring_boot.model.constants.CourseLevel;
import com.ra.base_spring_boot.repository.ICourseRepository;
import com.ra.base_spring_boot.services.ICourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements ICourseService {

    private final ICourseRepository courseRepository;

    @Override
    public CourseResponseDTO create(CourseRequestDTO dto) {
        Course course = Course.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .instructorName(dto.getInstructorName())
                .level(CourseLevel.valueOf(dto.getLevel().toUpperCase()))
                .createdAt(LocalDateTime.now())
                .build();
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
        course.setCreatedAt(LocalDateTime.now());
        course.setLevel(CourseLevel.valueOf(dto.getLevel().toUpperCase()));

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

    private CourseResponseDTO toDto(Course course) {
        return CourseResponseDTO.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .instructorName(course.getInstructorName())
                .level(course.getLevel().name())
                .createdAt(course.getCreatedAt())
                .build();
    }
}
