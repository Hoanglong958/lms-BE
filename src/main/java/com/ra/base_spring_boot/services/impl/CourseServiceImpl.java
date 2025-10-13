package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.Course.CourseRequestDTO;
import com.ra.base_spring_boot.dto.Course.CourseResponseDTO;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.Course;
import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.repository.ICourseRepository;
import com.ra.base_spring_boot.services.ICourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements ICourseService {

    private final ICourseRepository courseRepository;

    @Override
    public CourseResponseDTO create(CourseRequestDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User teacher = null;
        if (auth != null && auth.getPrincipal() instanceof User) {
            teacher = (User) auth.getPrincipal();
        }

        Course course = new Course();
        course.setName(dto.getName());
        course.setTitle(dto.getTitle());
        course.setDescription(dto.getDescription());
        course.setCategory(dto.getCategory());
        course.setTeacher(teacher);

        Course saved = courseRepository.save(course);
        return toDto(saved);
    }

    @Override
    public CourseResponseDTO update(Long id, CourseRequestDTO dto) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new HttpBadRequest("Course not found with id = " + id));

        course.setName(dto.getName());
        course.setTitle(dto.getTitle());
        course.setDescription(dto.getDescription());
        course.setCategory(dto.getCategory());

        Course saved = courseRepository.save(course);
        return toDto(saved);
    }

    @Override
    public void delete(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new HttpBadRequest("Course not found with id = " + id));
        courseRepository.delete(course);
    }

    @Override
    public CourseResponseDTO findById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new HttpBadRequest("Course not found with id = " + id));
        return toDto(course);
    }

    @Override
    public List<CourseResponseDTO> findAll() {
        return courseRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    // ✅ Hàm convert entity → DTO (bị thiếu trước đây)
    private CourseResponseDTO toDto(Course course) {
        Long teacherId = null;
        String teacherName = null;

        if (course.getTeacher() != null) {
            teacherId = course.getTeacher().getId();
            teacherName = course.getTeacher().getFullName(); // hoặc getUsername() nếu không có fullName
        }

        return CourseResponseDTO.builder()
                .id(course.getId())
                .name(course.getName())
                .title(course.getTitle())
                .description(course.getDescription())
                .category(course.getCategory())
                .teacherId(teacherId)
                .teacherName(teacherName)
                .build();
    }
}
