package com.ra.base_spring_boot.services.impl;


import com.ra.base_spring_boot.dto.ScheduleItem.ScheduleItemResponseDTO;
import com.ra.base_spring_boot.dto.ScheduleItem.GenerateScheduleRequestDTO;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.Course;
import com.ra.base_spring_boot.model.Period;
import com.ra.base_spring_boot.model.ScheduleItem;
import com.ra.base_spring_boot.repository.ICourseRepository;
import com.ra.base_spring_boot.repository.IPeriodRepository;
import com.ra.base_spring_boot.repository.IScheduleItemRepository;
import com.ra.base_spring_boot.services.IScheduleItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleItemServiceImpl implements IScheduleItemService {

    private final IScheduleItemRepository scheduleItemRepository;
    private final ICourseRepository courseRepository;
    private final IPeriodRepository periodRepository;

    @Override
    @Transactional
    public List<ScheduleItemResponseDTO> generateScheduleForCourse(GenerateScheduleRequestDTO req) {
        Long courseId = req.getCourseId();
        Course course = courseRepository.findById(java.util.Objects.requireNonNull(courseId, "courseId must not be null"))
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy course với id = " + courseId));

        Integer totalSessions = course.getTotalSessions();
        LocalDate startDate = course.getStartDate();
        if (totalSessions == null || totalSessions <= 0) {
            throw new HttpBadRequest("totalSessions của khóa học không hợp lệ.");
        }
        if (startDate == null) {
            throw new HttpBadRequest("startDate của khóa học phải được thiết lập.");
        }

        // Lấy periods: nếu client truyền danh sách id -> dùng; nếu không -> lấy toàn bộ active periods
        List<Period> periods;
        if (req.getPeriodIds() != null && !req.getPeriodIds().isEmpty()) {
            periods = periodRepository.findAllById(java.util.Objects.requireNonNull(req.getPeriodIds(), "periodIds must not be null"));
        } else {
            periods = periodRepository.findAll(); // hoặc findActive...
        }

        if (periods == null || periods.isEmpty()) {
            throw new HttpBadRequest("Không có ca học (period) để tạo thời khóa biểu.");
        }

        // Sắp xếp theo dayOfWeek (1..7) và startTime
        periods = periods.stream()
                .sorted(Comparator.comparingInt(Period::getDayOfWeek)
                        .thenComparing(Period::getStartTime))
                .collect(Collectors.toList());

        // Xoá lịch cũ cho khóa (tuỳ nghiệp vụ: ở đây mình xóa trước khi tạo schedule mới)
        scheduleItemRepository.deleteByCourseId(java.util.Objects.requireNonNull(courseId, "courseId must not be null"));

        List<ScheduleItem> created = new ArrayList<>();
        int createdCount = 0;
        int sessionNumber = 1;

        // Để duyệt ngày từ startDate tiến tới (limit an toàn: tránh vòng lặp vô hạn, maxWeeks = course.getWeeks() * 2)
        LocalDate cursor = startDate;
        int safetyLimitDays = Math.max(365, course.getWeeks() * 7 + 30); // an toàn

        int daysChecked = 0;
        while (createdCount < totalSessions && daysChecked <= safetyLimitDays) {
            // dayOfWeek: nếu Period dùng 1..7 (Monday=1), chuyển cursor.getDayOfWeek().getValue()
            int dow = cursor.getDayOfWeek().getValue(); // 1 (Mon) ... 7 (Sun)
            // Lấy period có dayOfWeek == dow
            List<Period> todaysPeriods = periods.stream()
                    .filter(p -> p.getDayOfWeek() == dow)
                    .collect(Collectors.toList());

            for (Period p : todaysPeriods) {
                if (createdCount >= totalSessions) break;

                LocalDateTime startAt = LocalDateTime.of(cursor, p.getStartTime());
                LocalDateTime endAt = LocalDateTime.of(cursor, p.getEndTime());

                ScheduleItem item = ScheduleItem.builder()
                        .course(course)
                        .period(p)
                        .sessionNumber(sessionNumber++)
                        .date(cursor)
                        .startAt(startAt)
                        .endAt(endAt)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .status("SCHEDULED")
                        .build();

                created.add(item);
                createdCount++;
            }

            // next day
            cursor = cursor.plus(1, ChronoUnit.DAYS);
            daysChecked++;
        }

        if (createdCount < totalSessions) {
            // Nếu chưa đủ, báo lỗi hoặc xử lý theo nghiệp vụ
            throw new HttpBadRequest("Không đủ ca học trong phạm vi để sinh đủ " + totalSessions + " buổi. Hãy mở rộng period hoặc chỉnh weeks/startDate.");
        }

        // Save all
        List<ScheduleItem> saved = scheduleItemRepository.saveAll(created);

        // Map to DTOs
        return saved.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<ScheduleItemResponseDTO> getScheduleByCourse(Long courseId) {
        return scheduleItemRepository.findByCourseIdOrderBySessionNumber(java.util.Objects.requireNonNull(courseId, "courseId must not be null"))
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void clearScheduleForCourse(Long courseId) {
        scheduleItemRepository.deleteByCourseId(java.util.Objects.requireNonNull(courseId, "courseId must not be null"));
    }

    private ScheduleItemResponseDTO toDto(ScheduleItem item) {
        return ScheduleItemResponseDTO.builder()
                .id(item.getId())
                .courseId(item.getCourse().getId())
                .periodId(item.getPeriod().getId())
                .sessionNumber(item.getSessionNumber())
                .date(item.getDate())
                .startAt(item.getStartAt())
                .endAt(item.getEndAt())
                .status(item.getStatus())
                .build();
    }
}

