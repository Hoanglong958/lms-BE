package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.Course.CourseRequestDTO;
import com.ra.base_spring_boot.dto.Course.CourseResponseDTO;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.Course;
import com.ra.base_spring_boot.model.constants.CourseLevel;
import com.ra.base_spring_boot.model.constants.CourseStatus;
import com.ra.base_spring_boot.repository.ICourseRepository;
import com.ra.base_spring_boot.services.ICourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
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
                .level(level)
                .totalSessions(dto.getTotalSessions())
                .startDate(dto.getStartDate())
                .createdAt(LocalDateTime.now())
                .build();

        // Tính endDate & status
        updateDateAndStatus(course);

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
        course.setLevel(parseLevel(dto.getLevel()));
        course.setTotalSessions(dto.getTotalSessions());
        course.setStartDate(dto.getStartDate());
        course.setUpdatedAt(LocalDateTime.now());

        updateDateAndStatus(course);

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

        updateDateAndStatus(course); // cập nhật status realtime

        return toDto(course);
    }

    @Override
    public List<CourseResponseDTO> findAll() {
        return courseRepository.findAll()
                .stream()
                .map(course -> {
                    updateDateAndStatus(course);
                    return toDto(course);
                })
                .toList();
    }

    @Override
    public Page<CourseResponseDTO> findAll(Pageable pageable) {
        return courseRepository.findAll(pageable)
                .map(course -> {
                    updateDateAndStatus(course);
                    return toDto(course);
                });
    }

    @Override
    public Page<CourseResponseDTO> search(String keyword, Pageable pageable) {
        String kw = keyword == null ? "" : keyword.trim();

        return courseRepository
                .findByTitleContainingIgnoreCase(kw, pageable) // ĐÃ BỎ instructorName
                .map(course -> {
                    updateDateAndStatus(course);
                    return toDto(course);
                });
    }

    // =========================== HELPER METHODS ===============================

    private void updateDateAndStatus(Course course) {
        if (course.getStartDate() == null || course.getTotalSessions() <= 0) {
            course.setStatus(CourseStatus.NOT_STARTED);
            return;
        }

        LocalDate start = course.getStartDate();
        LocalDate end = start.plusDays(course.getTotalSessions() - 1);

        course.setEndDate(end);

        LocalDate today = LocalDate.now();

        if (today.isBefore(start)) {
            course.setStatus(CourseStatus.NOT_STARTED);
        } else if (!today.isAfter(end)) {
            course.setStatus(CourseStatus.ONGOING);
        } else {
            course.setStatus(CourseStatus.ENDED);
        }
    }

    private CourseResponseDTO toDto(Course course) {
        return CourseResponseDTO.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .level(course.getLevel().name())
                .totalSessions(course.getTotalSessions())
                .startDate(course.getStartDate())
                .endDate(course.getEndDate())      // ✔ thêm vào response
                .status(course.getStatus().name()) // ✔ trạng thái
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
