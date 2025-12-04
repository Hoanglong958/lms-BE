package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.ScheduleItem.CreateManualScheduleRequestDTO;
import com.ra.base_spring_boot.dto.ScheduleItem.GenerateScheduleRequestDTO;
import com.ra.base_spring_boot.dto.ScheduleItem.ScheduleItemResponseDTO;
import com.ra.base_spring_boot.dto.ScheduleItem.UpdateScheduleItemRequestDTO;
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
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleItemServiceImpl implements IScheduleItemService {

    private final IScheduleItemRepository scheduleItemRepository;
    private final ICourseRepository courseRepository;
    private final IPeriodRepository periodRepository;

    // ======================================================================================
    // AUTO GENERATE — chỉ cần courseId, tự random ngày học và period
    // ======================================================================================
    @Override
    @Transactional
    public List<ScheduleItemResponseDTO> generateScheduleForCourse(GenerateScheduleRequestDTO req) {

        Long courseId = req.getCourseId();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy khóa học id = " + courseId));

        int totalSessions = course.getTotalSessions();
        int totalWeeks = course.getWeeks();
        LocalDate startDate = course.getStartDate();

        if (totalSessions <= 0) throw new HttpBadRequest("totalSessions không hợp lệ");
        if (totalWeeks <= 0) throw new HttpBadRequest("weeks của khóa học không hợp lệ");
        if (startDate == null) throw new HttpBadRequest("startDate của khóa học chưa được thiết lập");

        if (totalSessions % totalWeeks != 0) {
            throw new HttpBadRequest("totalSessions không chia đều cho weeks.");
        }

        int sessionsPerWeek = totalSessions / totalWeeks;

        // Ngày allowed: Thứ 2 → Thứ 6
        List<DayOfWeek> weekdays = List.of(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY
        );

        if (sessionsPerWeek > weekdays.size()) {
            throw new HttpBadRequest("Không thể xếp " + sessionsPerWeek + " buổi/tuần (chỉ có 5 ngày).");
        }

        // Lấy toàn bộ ca học
        List<Period> periods = periodRepository.findAll();
        if (periods.isEmpty()) throw new HttpBadRequest("Không có Period trong hệ thống.");

        // Xóa lịch cũ
        scheduleItemRepository.deleteByCourseId(courseId);

        Random rnd = new Random();
        List<ScheduleItem> result = new ArrayList<>();
        int sessionNumber = 1;

        // CHỌN MẪU 1 TUẦN: random ngày, random ca → giữ nguyên cho toàn bộ các tuần
        List<DayOfWeek> weekSampleDays = new ArrayList<>(weekdays);
        Collections.shuffle(weekSampleDays);
        weekSampleDays = weekSampleDays.subList(0, sessionsPerWeek);
        weekSampleDays.sort(Comparator.comparingInt(DayOfWeek::getValue));

        // map ngày → ca random
        Map<DayOfWeek, Period> chosenPeriodPerDay = new HashMap<>();
        for (DayOfWeek d : weekSampleDays) {
            Period p = periods.get(rnd.nextInt(periods.size()));
            chosenPeriodPerDay.put(d, p);
        }

        // Begin scheduling week-by-week
        for (int w = 0; w < totalWeeks; w++) {

            LocalDate weekStart = startDate.plusWeeks(w)
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));


            for (DayOfWeek dow : weekSampleDays) {

                if (sessionNumber > totalSessions) break;

                LocalDate date = weekStart.with(TemporalAdjusters.nextOrSame(dow));
                Period p = chosenPeriodPerDay.get(dow);

                ScheduleItem item = ScheduleItem.builder()
                        .course(course)
                        .period(p)
                        .sessionNumber(sessionNumber++)
                        .date(date)
                        .startAt(LocalDateTime.of(date, p.getStartTime()))
                        .endAt(LocalDateTime.of(date, p.getEndTime()))
                        .status("SCHEDULED")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                result.add(item);
            }
        }

        List<ScheduleItem> saved = scheduleItemRepository.saveAll(result);
        saved.sort(Comparator.comparingInt(ScheduleItem::getSessionNumber));
        return saved.stream().map(this::toDto).collect(Collectors.toList());
    }

    // ===================================================================
    // GET / CLEAR / UPDATE
    // ===================================================================
    @Override
    public List<ScheduleItemResponseDTO> getScheduleByCourse(Long courseId) {
        return scheduleItemRepository.findByCourseIdOrderBySessionNumber(courseId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void clearScheduleForCourse(Long courseId) {
        scheduleItemRepository.deleteByCourseId(courseId);
    }

    @Override
    @Transactional
    public ScheduleItemResponseDTO updateScheduleItem(Long scheduleItemId, UpdateScheduleItemRequestDTO req) {

        ScheduleItem item = scheduleItemRepository.findById(scheduleItemId)
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy ScheduleItem id = " + scheduleItemId));

        if (req.getDate() != null) item.setDate(req.getDate());

        if (req.getPeriodId() != null) {
            Period p = periodRepository.findById(req.getPeriodId())
                    .orElseThrow(() -> new HttpBadRequest("Không tìm thấy Period id = " + req.getPeriodId()));
            item.setPeriod(p);
            item.setStartAt(LocalDateTime.of(item.getDate(), p.getStartTime()));
            item.setEndAt(LocalDateTime.of(item.getDate(), p.getEndTime()));
        } else {
            Period p = item.getPeriod();
            item.setStartAt(LocalDateTime.of(item.getDate(), p.getStartTime()));
            item.setEndAt(LocalDateTime.of(item.getDate(), p.getEndTime()));
        }

        if (req.getStatus() != null) item.setStatus(req.getStatus());

        item.setUpdatedAt(LocalDateTime.now());
        return toDto(scheduleItemRepository.save(item));
    }

    // Mapper
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
    @Override
    @Transactional
    public List<ScheduleItemResponseDTO> createManualSchedule(CreateManualScheduleRequestDTO req) {

        Long courseId = req.getCourseId();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy khóa học id = " + courseId));

        if (course.getStartDate() == null)
            throw new HttpBadRequest("startDate của khóa học chưa được thiết lập.");

        int totalSessions = course.getTotalSessions();
        LocalDate startDate = course.getStartDate();

        // Validate
        if (req.getDaysOfWeek() == null || req.getDaysOfWeek().isEmpty())
            throw new HttpBadRequest("daysOfWeek không được bỏ trống!");

        if (req.getPeriodIds() == null || req.getPeriodIds().isEmpty())
            throw new HttpBadRequest("periodIds không được bỏ trống!");

        if (req.getDaysOfWeek().size() != req.getPeriodIds().size())
            throw new HttpBadRequest("daysOfWeek và periodIds phải có cùng số phần tử!");

        // Load Periods
        List<Period> periods = periodRepository.findAllById(req.getPeriodIds());
        if (periods.size() != req.getPeriodIds().size())
            throw new HttpBadRequest("Một hoặc nhiều periodIds không tồn tại.");

        // Xóa lịch cũ
        scheduleItemRepository.deleteByCourseId(courseId);

        List<ScheduleItem> result = new ArrayList<>();
        int sessionNumber = 1;

        // Tạo danh sách (DayOfWeek, Period) theo thứ tự ngày tăng dần
        List<Map.Entry<DayOfWeek, Period>> dayPeriodPairs =
                new ArrayList<>();

        for (int i = 0; i < req.getDaysOfWeek().size(); i++) {
            DayOfWeek dow = DayOfWeek.of(req.getDaysOfWeek().get(i));
            Period p = periods.get(i);
            dayPeriodPairs.add(Map.entry(dow, p));
        }

        // Sort để đảm bảo sessionNumber theo đúng thứ trong tuần
        dayPeriodPairs.sort(Comparator.comparingInt(e -> e.getKey().getValue()));

        // Sinh lịch theo tuần
        int weekOffset = 0;
        while (sessionNumber <= totalSessions) {

            LocalDate weekStart = startDate.plusWeeks(weekOffset)
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

            for (var entry : dayPeriodPairs) {

                if (sessionNumber > totalSessions) break;

                DayOfWeek dow = entry.getKey();
                Period p = entry.getValue();

                LocalDate date = weekStart.with(TemporalAdjusters.nextOrSame(dow));

                ScheduleItem item = ScheduleItem.builder()
                        .course(course)
                        .period(p)
                        .sessionNumber(sessionNumber++)
                        .date(date)
                        .startAt(LocalDateTime.of(date, p.getStartTime()))
                        .endAt(LocalDateTime.of(date, p.getEndTime()))
                        .status("SCHEDULED")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                result.add(item);
            }

            weekOffset++;
        }

        List<ScheduleItem> saved = scheduleItemRepository.saveAll(result);
        saved.sort(Comparator.comparingInt(ScheduleItem::getSessionNumber));
        return saved.stream().map(this::toDto).collect(Collectors.toList());
    }

}
