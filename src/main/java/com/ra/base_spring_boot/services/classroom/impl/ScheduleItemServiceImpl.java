package com.ra.base_spring_boot.services.classroom.impl;

import com.ra.base_spring_boot.dto.ScheduleItem.*;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.*;
import com.ra.base_spring_boot.model.Class;
import com.ra.base_spring_boot.repository.classroom.IClassRepository;
import com.ra.base_spring_boot.repository.classroom.IPeriodRepository;
import com.ra.base_spring_boot.repository.classroom.IScheduleItemRepository;
import com.ra.base_spring_boot.repository.course.IClassCourseRepository;
import com.ra.base_spring_boot.repository.course.ICourseRepository;
import com.ra.base_spring_boot.services.classroom.IScheduleItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ScheduleItemServiceImpl implements IScheduleItemService {

        private final IScheduleItemRepository scheduleItemRepository;
        private final ICourseRepository courseRepository;
        private final IClassRepository classRepository;
        private final IPeriodRepository periodRepository;
        private final IClassCourseRepository classCourseRepository;

        // =========================================================
        // 1. AUTO GENERATE
        // =========================================================
        @Override
        @Transactional
        public List<ScheduleItemResponseDTO> generateScheduleForCourse(GenerateScheduleRequestDTO req) {

                Course course = courseRepository.findById(req.getCourseId())
                                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy course"));

                Class clazz = classRepository.findById(req.getClassId())
                                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy class"));

                if (clazz.getStartDate() == null)
                        throw new HttpBadRequest("Class chưa có startDate");

                ClassCourse classCourse = classCourseRepository
                                .findByClazz_IdAndCourse_Id(clazz.getId(), course.getId())
                                .orElseThrow(() -> new HttpBadRequest("ClassCourse chưa tồn tại"));

                List<Period> periods = periodRepository.findAll();
                if (periods.isEmpty())
                        throw new HttpBadRequest("Chưa có period");

                scheduleItemRepository.deleteByClassCourse_Id(classCourse.getId());

                List<DayOfWeek> weekdays = List.of(
                                DayOfWeek.MONDAY,
                                DayOfWeek.TUESDAY,
                                DayOfWeek.WEDNESDAY,
                                DayOfWeek.THURSDAY,
                                DayOfWeek.FRIDAY);

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

                        for (DayOfWeek dow : days) {
                                if (sessionNumber > course.getTotalSessions())
                                        break;

                                Period p = periods.get(rnd.nextInt(periods.size()));
                                LocalDate date = weekStart.with(TemporalAdjusters.nextOrSame(dow));

                                result.add(buildItem(classCourse, p, date, sessionNumber++));
                        }
                        weekIndex++;
                }

                return scheduleItemRepository.saveAll(result)
                                .stream().map(this::toDto).toList();
        }

        // =========================================================
        // 2. MANUAL CREATE
        // =========================================================
        @Override
        @Transactional
        public List<ScheduleItemResponseDTO> createManualSchedule(CreateManualScheduleRequestDTO req) {

                Course course = courseRepository.findById(req.getCourseId())
                                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy course"));

                Class clazz = classRepository.findById(req.getClassId())
                                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy class"));

                ClassCourse classCourse = classCourseRepository
                                .findByClazz_IdAndCourse_Id(clazz.getId(), course.getId())
                                .orElseThrow(() -> new HttpBadRequest("ClassCourse chưa tồn tại"));

                scheduleItemRepository.deleteByClassCourse_Id(classCourse.getId());

                List<ScheduleItem> result = new ArrayList<>();
                int sessionNumber = 1;
                int week = 0;

                while (sessionNumber <= course.getTotalSessions()) {

                        LocalDate weekStart = clazz.getStartDate()
                                        .plusWeeks(week)
                                        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

                        for (int i = 0; i < req.getDaysOfWeek().size(); i++) {
                                if (sessionNumber > course.getTotalSessions())
                                        break;

                                DayOfWeek dow = DayOfWeek.of(req.getDaysOfWeek().get(i));
                                Period p = periodRepository.findById(req.getPeriodIds().get(i))
                                                .orElseThrow(() -> new HttpBadRequest("Period không tồn tại"));

                                LocalDate date = weekStart.with(TemporalAdjusters.nextOrSame(dow));
                                result.add(buildItem(classCourse, p, date, sessionNumber++));
                        }
                        week++;
                }

                return scheduleItemRepository.saveAll(result)
                                .stream().map(this::toDto).toList();
        }

        // =========================================================
        // 3. GET BY CLASS COURSE
        // =========================================================
        @Override
        @Transactional(readOnly = true)
        public ClassScheduleResponseDTO getScheduleByClassCourse(Long classCourseId) {

                ClassCourse cc = classCourseRepository.findById(classCourseId)
                                .orElseThrow(() -> new HttpBadRequest("ClassCourse không tồn tại"));

                List<ScheduleItemResponseDTO> schedules = scheduleItemRepository
                                .findScheduleDetailByClassCourse(classCourseId)
                                .stream().map(this::toDto).toList();

                return ClassScheduleResponseDTO.builder()
                                .classCourseId(cc.getId())
                                .courseId(cc.getCourse().getId())
                                .schedules(schedules)
                                .build();
        }

        // =========================================================
        // 4. CLEAR
        // =========================================================
        @Override
        @Transactional
        public void clearScheduleForCourse(Long classCourseId) {
                scheduleItemRepository.deleteByClassCourse_Id(classCourseId);
        }

        // =========================================================
        // 5. UPDATE
        // =========================================================
        @Override
        @Transactional
        public ScheduleItemResponseDTO updateScheduleItem(Long id, UpdateScheduleItemRequestDTO req) {

                ScheduleItem item = scheduleItemRepository.findById(id)
                                .orElseThrow(() -> new HttpBadRequest("ScheduleItem không tồn tại"));

                if (req.getPeriodId() != null) {
                        Period p = periodRepository.findById(req.getPeriodId())
                                        .orElseThrow(() -> new HttpBadRequest("Period không tồn tại"));
                        item.setPeriod(p);
                }

                if (req.getDate() != null) {
                        item.setDate(req.getDate());
                }

                if (req.getStatus() != null) {
                        item.setStatus(req.getStatus());
                }

                item.setUpdatedAt(LocalDateTime.now());
                return toDto(scheduleItemRepository.save(item));
        }

        @Override
        @Transactional(readOnly = true)
        public List<ScheduleItemResponseDTO> getScheduleByCourse(Long courseId) {

                // kiểm tra course tồn tại
                courseRepository.findById(courseId)
                                .orElseThrow(() -> new HttpBadRequest("Course không tồn tại"));

                return scheduleItemRepository
                                .findByClassCourse_Course_IdOrderByDateAscSessionNumberAsc(courseId)
                                .stream()
                                .map(this::toDto)
                                .toList();
        }

        @Override
        @Transactional(readOnly = true)
        public List<ScheduleItemResponseDTO> getScheduleByClass(Long classId) {

                // kiểm tra class tồn tại
                classRepository.findById(classId)
                                .orElseThrow(() -> new HttpBadRequest("Class không tồn tại"));

                return scheduleItemRepository
                                .findByClassCourse_Clazz_IdOrderByDateAscSessionNumberAsc(classId)
                                .stream()
                                .map(this::toDto)
                                .toList();
        }

        // =========================================================
        // 6. UPDATE WEEK SCHEDULE - Cập nhật lịch theo tuần cụ thể
        // =========================================================
        @Override
        @Transactional
        public List<ScheduleItemResponseDTO> updateWeekSchedule(UpdateWeekScheduleRequestDTO req) {
                // Validate classCourse exists
                ClassCourse classCourse = classCourseRepository.findById(req.getClassCourseId())
                                .orElseThrow(() -> new HttpBadRequest("ClassCourse không tồn tại"));

                // Get existing schedule items for this specific week
                List<ScheduleItem> existingWeekItems = scheduleItemRepository
                                .findByClassCourse_IdOrderBySessionNumber(req.getClassCourseId())
                                .stream()
                                .filter(item -> {
                                        LocalDate itemDate = item.getDate();
                                        return !itemDate.isBefore(req.getWeekStartDate())
                                                        && !itemDate.isAfter(req.getWeekEndDate());
                                })
                                .toList();

                // Create a map of existing items for easy lookup
                Map<String, ScheduleItem> existingItemsMap = new HashMap<>();
                for (ScheduleItem item : existingWeekItems) {
                        String key = item.getDate().toString() + "_" + item.getPeriod().getId();
                        existingItemsMap.put(key, item);
                }

                List<ScheduleItem> itemsToSave = new ArrayList<>();

                // Process each schedule item from request
                if (req.getScheduleItems() != null) {
                        for (UpdateWeekScheduleRequestDTO.WeekScheduleItem weekItem : req.getScheduleItems()) {
                                if (weekItem.getDate() == null || weekItem.getPeriodId() == null) {
                                        continue; // Skip invalid items
                                }

                                Period period = periodRepository.findById(weekItem.getPeriodId().longValue())
                                                .orElseThrow(() -> new HttpBadRequest(
                                                                "Period không tồn tại: " + weekItem.getPeriodId()));

                                String key = weekItem.getDate().toString() + "_" + weekItem.getPeriodId();
                                ScheduleItem existingItem = existingItemsMap.get(key);

                                if (existingItem != null) {
                                        // Update existing item
                                        existingItem.setPeriod(period);
                                        existingItem.setDate(weekItem.getDate());
                                        existingItem.setStartAt(
                                                        LocalDateTime.of(weekItem.getDate(), period.getStartTime()));
                                        existingItem.setEndAt(
                                                        LocalDateTime.of(weekItem.getDate(), period.getEndTime()));
                                        existingItem.setUpdatedAt(LocalDateTime.now());
                                        itemsToSave.add(existingItem);
                                        existingItemsMap.remove(key); // Remove from map so it won't be deleted
                                } else {
                                        // Create new item
                                        // Find the next available session number
                                        int maxSessionNumber = scheduleItemRepository
                                                        .findByClassCourse_IdOrderBySessionNumber(
                                                                        req.getClassCourseId())
                                                        .stream()
                                                        .mapToInt(ScheduleItem::getSessionNumber)
                                                        .max()
                                                        .orElse(0);

                                        ScheduleItem newItem = ScheduleItem.builder()
                                                        .classCourse(classCourse)
                                                        .period(period)
                                                        .sessionNumber(maxSessionNumber + 1)
                                                        .date(weekItem.getDate())
                                                        .startAt(LocalDateTime.of(weekItem.getDate(),
                                                                        period.getStartTime()))
                                                        .endAt(LocalDateTime.of(weekItem.getDate(),
                                                                        period.getEndTime()))
                                                        .status("SCHEDULED")
                                                        .createdAt(LocalDateTime.now())
                                                        .updatedAt(LocalDateTime.now())
                                                        .build();
                                        itemsToSave.add(newItem);
                                }
                        }
                }

                // Delete remaining items in the week that weren't in the request
                for (ScheduleItem itemToDelete : existingItemsMap.values()) {
                        scheduleItemRepository.delete(itemToDelete);
                }

                // Save all updates and new items
                List<ScheduleItem> savedItems = scheduleItemRepository.saveAll(itemsToSave);

                // Return updated schedule for the week
                return savedItems.stream()
                                .map(this::toDto)
                                .toList();
        }

        // =========================================================
        // HELPER
        // =========================================================
        private ScheduleItem buildItem(
                        ClassCourse classCourse,
                        Period p,
                        LocalDate date,
                        int sessionNumber) {
                return ScheduleItem.builder()
                                .classCourse(classCourse)
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
                                .courseId(item.getClassCourse().getCourse().getId())
                                .classId(item.getClassCourse().getClazz().getId())
                                .periodId(item.getPeriod().getId())
                                .sessionNumber(item.getSessionNumber())
                                .date(item.getDate())
                                .startAt(item.getStartAt())
                                .endAt(item.getEndAt())
                                .status(item.getStatus())
                                .build();
        }

        // =========================================================
        // 7. GET SCHEDULED DATES FOR A CLASS BY YEAR/MONTH
        // =========================================================
        @Override
        @Transactional(readOnly = true)
        public List<java.time.LocalDate> getScheduledDatesForClass(Long classId, int year, int month) {
                return scheduleItemRepository.findScheduledDatesByClassAndMonth(classId, year, month);
        }
}
