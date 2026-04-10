package com.ra.base_spring_boot.services.course.impl;

import com.ra.base_spring_boot.dto.Course.CourseRequestDTO;
import com.ra.base_spring_boot.dto.Course.CourseResponseDTO;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.Course;
import com.ra.base_spring_boot.model.constants.CourseLevel;
import com.ra.base_spring_boot.repository.course.ICourseRepository;
import com.ra.base_spring_boot.repository.course.IClassCourseRepository;
import com.ra.base_spring_boot.services.course.ICourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.ra.base_spring_boot.utils.SecurityUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements ICourseService {

    private final ICourseRepository courseRepository;
    private final IClassCourseRepository classCourseRepository;

    @Override
    public CourseResponseDTO create(CourseRequestDTO dto) {

        CourseLevel level = parseLevel(dto.getLevel());

        Course course = Course.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .level(level)
                .imageUrl(dto.getImageUrl())
                .totalSessions(dto.getTotalSessions())
                .tuitionFee(dto.getTuitionFee() != null ? dto.getTuitionFee() : java.math.BigDecimal.ZERO)
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
        course.setTuitionFee(dto.getTuitionFee() != null ? dto.getTuitionFee() : java.math.BigDecimal.ZERO);
        course.setUpdatedAt(LocalDateTime.now());

        courseRepository.save(java.util.Objects.requireNonNull(course, "course must not be null"));

        return toDto(course);
    }

    @Override
    public void delete(Long id) {
        Course course = courseRepository.findById(Objects.requireNonNull(id, "id must not be null"))
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy khóa học với id = " + id));
        course.setIsActive(false);
        courseRepository.save(java.util.Objects.requireNonNull(course, "course must not be null"));
    }

    @Override
    public void toggleActive(Long id) {
        Course course = courseRepository.findById(Objects.requireNonNull(id, "id must not be null"))
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy khóa học với id = " + id));
        boolean current = Boolean.TRUE.equals(course.getIsActive());
        course.setIsActive(!current);
        courseRepository.save(java.util.Objects.requireNonNull(course, "course must not be null"));
    }

    @Override
    public CourseResponseDTO findById(Long id) {
        Course course = courseRepository.findById(Objects.requireNonNull(id, "id must not be null"))
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy khóa học với id = " + id));
        if (!SecurityUtils.isAdmin() && Boolean.FALSE.equals(course.getIsActive())) {
            throw new HttpBadRequest("Không tìm thấy khóa học với id = " + id);
        }
        if (!SecurityUtils.isAdmin() && !classCourseRepository.existsByCourse_Id(course.getId())) {
            throw new HttpBadRequest("Không tìm thấy khóa học với id = " + id);
        }

        return toDto(course);
    }

    @Override
    public List<CourseResponseDTO> findAll() {
        List<Course> courses = SecurityUtils.isAdmin()
                ? courseRepository.findAll()
                : courseRepository.findActiveAndAssigned();
        return courses
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public Page<CourseResponseDTO> findAll(Pageable pageable) {
        Page<Course> page = SecurityUtils.isAdmin()
                ? courseRepository.findAll(pageable)
                : courseRepository.findActiveAndAssigned(pageable);
        return page
                .map(this::toDto);
    }

    @Override
    public Page<CourseResponseDTO> search(String keyword, Pageable pageable) {
        String kw = keyword == null ? "" : keyword.trim();
        Page<Course> page = SecurityUtils.isAdmin()
                ? courseRepository.findByTitleContainingIgnoreCase(kw, pageable)
                : courseRepository.searchActiveAssignedByTitle(kw, pageable);
        return page.map(this::toDto);
    }

    @Override
    public Page<CourseResponseDTO> findByStatus(Long studentId, String keyword, String status, Pageable pageable) {
        String kw = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();
        String st = (status == null || status.equalsIgnoreCase("ALL")) ? null : status.toUpperCase();
        
        com.ra.base_spring_boot.model.constants.PaymentStatus statusEnum = null;
        if (st != null && !st.equals("NONE")) {
            try {
                statusEnum = com.ra.base_spring_boot.model.constants.PaymentStatus.valueOf(st);
            } catch (IllegalArgumentException e) {
                // Ignore invalid status
            }
        }
        
        Boolean isActive = SecurityUtils.isAdmin() ? null : true;

        boolean requireAssignment = !SecurityUtils.isAdmin();
        Page<Course> page = courseRepository.findWithRegistrationStatus(studentId, kw, st, statusEnum, isActive, requireAssignment, pageable);
        return page.map(this::toDto);
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
                .tuitionFee(course.getTuitionFee())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .isActive(Boolean.TRUE.equals(course.getIsActive()))
                .build();
    }

    // Local isAdmin removed in favor of SecurityUtils

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
