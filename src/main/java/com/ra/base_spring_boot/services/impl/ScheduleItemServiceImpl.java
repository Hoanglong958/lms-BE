package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.ScheduleItem.*;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.*;
import com.ra.base_spring_boot.model.Class;
import com.ra.base_spring_boot.repository.*;
import com.ra.base_spring_boot.services.IScheduleItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleItemServiceImpl implements IScheduleItemService {

    private final IScheduleItemRepository scheduleItemRepository;
    private final ICourseRepository courseRepository;
    private final IPeriodRepository periodRepository;
    private final IClassRepository classRepository;
    private final IClassCourseRepository classCourseRepository;

    // =========================================================================
    // 1. AUTO GENERATE
    // =========================================================================
    @Override
    @Transactional
    public List<ScheduleItemResponseDTO> generateScheduleForCourse(GenerateScheduleRequestDTO req) {

        Course course = courseRepository.findById(req.getCourseId())
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy course id = " + req.getCourseId()));

        Class clazz = classRepository.findById(req.getClassId())
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy class id = " + req.getClassId()));

        if (clazz.getStartDate() == null)
            throw new HttpBadRequest("Lớp học chưa có startDate");

        if (course.getTotalSessions() <= 0)
            throw new HttpBadRequest("totalSessions không hợp lệ");

        if (req.getSessionsPerWeek() <= 0 || req.getSessionsPerWeek() > 5)
            throw new HttpBadRequest("sessionsPerWeek phải từ 1–5");

        List<Period> periods = periodRepository.findAll();
        if (periods.isEmpty())
            throw new HttpBadRequest("Chưa có ca học (Period)");

        scheduleItemRepository.deleteByCourse_IdAndClazz_Id(course.getId(), clazz.getId());

        List<DayOfWeek> weekdays = List.of(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY
        );

        Random rnd = new Random();
        List<ScheduleItem> result = new ArrayList<>();

        int sessionNumber = 1;
        int weekIndex = 0;

        while (sessionNumber <= course.getTotalSessions()) {

            LocalDate weekStart = clazz.getStartDate()
                    .plusWeeks(weekIndex)
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

            List<DayOfWeek> days = new ArrayList<>(weekdays);
            Collections.shuffle(days);
            days = days.subList(0, req.getSessionsPerWeek());
            days.sort(Comparator.comparingInt(DayOfWeek::getValue));

            for (DayOfWeek dow : days) {
                if (sessionNumber > course.getTotalSessions()) break;

                Period p = periods.get(rnd.nextInt(periods.size()));
                LocalDate date = weekStart.with(TemporalAdjusters.nextOrSame(dow));

                result.add(buildItem(course, clazz, p, date, sessionNumber++));
            }
            weekIndex++;
        }

        return scheduleItemRepository.saveAll(result)
                .stream().map(this::toDto).toList();
    }

    // =========================================================================
    // 2. GET
    // =========================================================================
    @Override
    public List<ScheduleItemResponseDTO> getScheduleByCourse(Long courseId) {
        return scheduleItemRepository.findByCourseIdOrderBySessionNumber(courseId)
                .stream().map(this::toDto).toList();
    }

    @Override
    public List<ScheduleItemResponseDTO> getScheduleByCourseAndClass(Long courseId, Long classId) {
        return scheduleItemRepository
                .findByCourse_IdAndClazz_IdOrderBySessionNumber(courseId, classId)
                .stream().map(this::toDto).toList();
    }

    @Override
    public List<ScheduleItemResponseDTO> getScheduleByCourseAndClassFiltered(
            Long courseId,
            Long classId,
            String status,
            LocalDate from,
            LocalDate to,
            Long periodId
    ) {

        // validate cơ bản
        if (courseId == null || classId == null) {
            throw new HttpBadRequest("courseId và classId không được để trống");
        }

        // validate date range
        if (from != null && to != null && from.isAfter(to)) {
            throw new HttpBadRequest("fromDate không được lớn hơn toDate");
        }

        return scheduleItemRepository
                .findByCourseClassWithFilters(
                        courseId,
                        classId,
                        status,
                        from,
                        to,
                        periodId
                )
                .stream()
                .map(this::toDto)
                .toList();
    }


    // =========================================================================
    // 3. CLEAR
    // =========================================================================
    @Override
    @Transactional
    public void clearScheduleForCourse(Long courseId) {
        scheduleItemRepository.deleteByCourseId(courseId);
    }

    // =========================================================================
    // 4. UPDATE
    // =========================================================================
    @Override
    @Transactional
    public ScheduleItemResponseDTO updateScheduleItem(Long id, UpdateScheduleItemRequestDTO req) {

        ScheduleItem item = scheduleItemRepository.findById(id)
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy scheduleItem id = " + id));

        if (req.getClassId() != null) {
            Class clazz = classRepository.findById(req.getClassId())
                    .orElseThrow(() -> new HttpBadRequest("Không tìm thấy class id"));
            item.setClazz(clazz);
        }

        if (req.getPeriodId() != null) {
            Period p = periodRepository.findById(req.getPeriodId())
                    .orElseThrow(() -> new HttpBadRequest("Không tìm thấy period id"));
            item.setPeriod(p);

            if (item.getDate() != null) {
                item.setStartAt(LocalDateTime.of(item.getDate(), p.getStartTime()));
                item.setEndAt(LocalDateTime.of(item.getDate(), p.getEndTime()));
            }
        }

        if (req.getDate() != null) {
            item.setDate(req.getDate());
            if (item.getPeriod() != null) {
                item.setStartAt(LocalDateTime.of(req.getDate(), item.getPeriod().getStartTime()));
                item.setEndAt(LocalDateTime.of(req.getDate(), item.getPeriod().getEndTime()));
            }
        }

        if (req.getStatus() != null)
            item.setStatus(req.getStatus());

        item.setUpdatedAt(LocalDateTime.now());
        return toDto(scheduleItemRepository.save(item));
    }

    // =========================================================================
    // 5. MANUAL CREATE (FIX ĐÚNG NGHIỆP VỤ)
    // =========================================================================
    @Override
    @Transactional
    public List<ScheduleItemResponseDTO> createManualSchedule(CreateManualScheduleRequestDTO req) {

        Course course = courseRepository.findById(req.getCourseId())
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy course"));

        Class clazz = classRepository.findById(req.getClassId())
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy class"));

        if (clazz.getStartDate() == null)
            throw new HttpBadRequest("Class chưa có startDate");

        if (req.getDaysOfWeek().size() != req.getPeriodIds().size())
            throw new HttpBadRequest("daysOfWeek và periodIds phải cùng số lượng");

        // validate day of week
        for (Integer d : req.getDaysOfWeek()) {
            if (d < 1 || d > 7)
                throw new HttpBadRequest("daysOfWeek phải từ 1 đến 7");
        }

        // validate period tồn tại (CHO PHÉP TRÙNG)
        Set<Long> uniquePeriodIds = new HashSet<>(req.getPeriodIds());
        List<Period> periodList = periodRepository.findAllById(uniquePeriodIds);

        if (periodList.size() != uniquePeriodIds.size())
            throw new HttpBadRequest("Có periodId không tồn tại");

        Map<Long, Period> periodMap = periodList.stream()
                .collect(Collectors.toMap(Period::getId, p -> p));

        scheduleItemRepository.deleteByCourse_IdAndClazz_Id(course.getId(), clazz.getId());

        List<ScheduleItem> result = new ArrayList<>();
        int sessionNumber = 1;
        int week = 0;

        while (sessionNumber <= course.getTotalSessions()) {

            LocalDate weekStart = clazz.getStartDate()
                    .plusWeeks(week)
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

            for (int i = 0; i < req.getDaysOfWeek().size(); i++) {
                if (sessionNumber > course.getTotalSessions()) break;

                DayOfWeek dow = DayOfWeek.of(req.getDaysOfWeek().get(i));
                Period p = periodMap.get(req.getPeriodIds().get(i));
                LocalDate date = weekStart.with(TemporalAdjusters.nextOrSame(dow));

                result.add(buildItem(course, clazz, p, date, sessionNumber++));
            }
            week++;
        }

        return scheduleItemRepository.saveAll(result)
                .stream().map(this::toDto).toList();
    }

    @Override
    public ClassScheduleResponseDTO getScheduleByClassCourse(Long classCourseId) {

        // 1. Lấy class_course
        ClassCourse classCourse = classCourseRepository.findById(classCourseId)
                .orElseThrow(() ->
                        new HttpBadRequest("Không tìm thấy class_course id = " + classCourseId)
                );

        Class clazz = classCourse.getClazz();
        Course course = classCourse.getCourse();

        // 2. Lấy thời khóa biểu theo class + course
        List<ScheduleItem> scheduleItems =
                scheduleItemRepository
                        .findByCourse_IdAndClazz_IdOrderBySessionNumber(
                                course.getId(),
                                clazz.getId()
                        );

        // 3. Map sang DTO
        return ClassScheduleResponseDTO.builder()
                .classCourseId(classCourseId)
                .classId(clazz.getId())
                .className(clazz.getClassName())
                .courseId(course.getId())
                .courseName(course.getTitle())
                .schedules(
                        scheduleItems.stream()
                                .map(this::toDto)
                                .toList()
                )
                .build();
    }



    // =========================================================================
    // HELPER
    // =========================================================================
    private ScheduleItem buildItem(Course course, Class clazz, Period p,
                                   LocalDate date, int sessionNumber) {

        return ScheduleItem.builder()
                .course(course)
                .clazz(clazz)
                .period(p)
                .sessionNumber(sessionNumber)
                .date(date)
                .startAt(LocalDateTime.of(date, p.getStartTime()))
                .endAt(LocalDateTime.of(date, p.getEndTime()))
                .status("SCHEDULED")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private ScheduleItemResponseDTO toDto(ScheduleItem item) {
        return ScheduleItemResponseDTO.builder()
                .id(item.getId())
                .courseId(item.getCourse().getId())
                .classId(item.getClazz().getId())
                .periodId(item.getPeriod().getId())
                .sessionNumber(item.getSessionNumber())
                .date(item.getDate())
                .startAt(item.getStartAt())
                .endAt(item.getEndAt())
                .status(item.getStatus())
                .build();
    }


}
