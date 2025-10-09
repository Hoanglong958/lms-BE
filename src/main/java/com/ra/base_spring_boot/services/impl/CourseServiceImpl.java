package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.req.CourseCreateReq;
import com.ra.base_spring_boot.dto.req.CourseUpdateReq;
import com.ra.base_spring_boot.dto.resp.CourseResp;
import com.ra.base_spring_boot.entity.Course;
import com.ra.base_spring_boot.repository.ICourseRepository;
import com.ra.base_spring_boot.repository.IEntityUserRepository;
import com.ra.base_spring_boot.services.ICourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements ICourseService {

    private final ICourseRepository courseRepository;
    private final IEntityUserRepository userRepository;

    private CourseResp mapToResp(Course c) {
        return CourseResp.builder()
                .id(c.getCourseId())
                .title(c.getTitle())
                .description(c.getDescription())
                .category(c.getCategory())
                .teacherId(c.getTeacher() != null ? c.getTeacher().getUserId() : null)
                .teacherUsername(c.getTeacher() != null ? c.getTeacher().getName() : null)
                .build();
    }

    @Override
    @Transactional
    public CourseResp create(CourseCreateReq req) {
        var teacher = userRepository.findById(req.getTeacherId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher not found"));
        var c = new Course();
        c.setTitle(req.getTitle());
        c.setDescription(req.getDescription());
        c.setCategory(req.getCategory());
        c.setTeacher(teacher);
        c = courseRepository.save(c);
        return mapToResp(c);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResp get(Integer id) {
        var c = courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        return mapToResp(c);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseResp> search(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            var page = courseRepository.findAll(pageable);
            return page.map(this::mapToResp);
        }
        var page = courseRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(keyword, keyword, pageable);
        return page.map(this::mapToResp);
    }

    @Override
    @Transactional
    public CourseResp update(Integer id, CourseUpdateReq req) {
        var c = courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        if (req.getTitle() != null) c.setTitle(req.getTitle());
        if (req.getDescription() != null) c.setDescription(req.getDescription());
        if (req.getCategory() != null) c.setCategory(req.getCategory());
        if (req.getTeacherId() != null) {
            var teacher = userRepository.findById(req.getTeacherId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher not found"));
            c.setTeacher(teacher);
        }
        c = courseRepository.save(c);
        return mapToResp(c);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!courseRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found");
        }
        courseRepository.deleteById(id);
    }
}

