    package com.ra.base_spring_boot.services.impl;

    import com.ra.base_spring_boot.dto.ScheduleItem.CreateManualScheduleRequestDTO;
    import com.ra.base_spring_boot.dto.ScheduleItem.GenerateScheduleRequestDTO;
    import com.ra.base_spring_boot.dto.ScheduleItem.ScheduleItemResponseDTO;
    import com.ra.base_spring_boot.dto.ScheduleItem.UpdateScheduleItemRequestDTO;
    import com.ra.base_spring_boot.exception.HttpBadRequest;
    import com.ra.base_spring_boot.model.Class;
    import com.ra.base_spring_boot.model.Course;
    import com.ra.base_spring_boot.model.Period;
    import com.ra.base_spring_boot.model.ScheduleItem;
    import com.ra.base_spring_boot.repository.IClassRepository;
    import com.ra.base_spring_boot.repository.ICourseRepository;
    import com.ra.base_spring_boot.repository.IPeriodRepository;
    import com.ra.base_spring_boot.repository.IScheduleItemRepository;
    import com.ra.base_spring_boot.services.IScheduleItemService;
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
        private final IPeriodRepository periodRepository;
        private final IClassRepository classRepository; // 👈 BẠN QUÊN IMPORT

        // =========================================================================
        // 1. AUTO-GENERATE
        // =========================================================================
        @Override
        @Transactional
        public List<ScheduleItemResponseDTO> generateScheduleForCourse(GenerateScheduleRequestDTO req) {

            Course course = courseRepository.findById(req.getCourseId())
                    .orElseThrow(() -> new HttpBadRequest("Không tìm thấy khóa học id = " + req.getCourseId()));

            Class clazz = classRepository.findById(req.getClassId())
                    .orElseThrow(() -> new HttpBadRequest("Không tìm thấy lớp id = " + req.getClassId()));

            int sessionsPerWeek = req.getSessionsPerWeek();
            int totalSessions = course.getTotalSessions();
            LocalDate startDate = course.getStartDate();

            if (totalSessions <= 0) throw new HttpBadRequest("totalSessions không hợp lệ");
            if (startDate == null) throw new HttpBadRequest("startDate của khóa học chưa thiết lập");
            if (sessionsPerWeek > 5) throw new HttpBadRequest("sessionsPerWeek không được quá 5");

            // weekday T2–T6
            List<DayOfWeek> weekdays = List.of(
                    DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY
            );

            List<Period> periods = periodRepository.findAll();
            if (periods.isEmpty()) throw new HttpBadRequest("Hệ thống chưa có Period.");

            scheduleItemRepository.deleteByCourseId(course.getId());

            Random rnd = new Random();
            List<ScheduleItem> result = new ArrayList<>();
            int sessionNumber = 1;
            int weekIndex = 0;

            // pattern tuần
            List<DayOfWeek> baseDays = new ArrayList<>(weekdays);
            Collections.shuffle(baseDays);
            baseDays = baseDays.subList(0, sessionsPerWeek);
            baseDays.sort(Comparator.comparingInt(DayOfWeek::getValue));

            Map<DayOfWeek, Period> baseDayPeriod = new HashMap<>();
            for (DayOfWeek d : baseDays) {
                baseDayPeriod.put(d, periods.get(rnd.nextInt(periods.size())));
            }

            while (sessionNumber <= totalSessions) {

                int remaining = totalSessions - (sessionNumber - 1);
                int sessionsThisWeek = Math.min(remaining, sessionsPerWeek);

                List<DayOfWeek> wDays;
                Map<DayOfWeek, Period> wMap = new HashMap<>();

                if (sessionsThisWeek == sessionsPerWeek) {
                    wDays = baseDays;
                    wMap = baseDayPeriod;
                } else {
                    List<DayOfWeek> tmp = new ArrayList<>(weekdays);
                    Collections.shuffle(tmp);
                    wDays = tmp.subList(0, sessionsThisWeek);
                    wDays.sort(Comparator.comparingInt(DayOfWeek::getValue));

                    for (DayOfWeek d : wDays) {
                        wMap.put(d, periods.get(rnd.nextInt(periods.size())));
                    }
                }

                LocalDate weekStart = startDate.plusWeeks(weekIndex)
                        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

                for (DayOfWeek dow : wDays) {
                    if (sessionNumber > totalSessions) break;

                    LocalDate date = weekStart.with(TemporalAdjusters.nextOrSame(dow));
                    Period p = wMap.get(dow);

                    ScheduleItem item = ScheduleItem.builder()
                            .course(course)
                            .clazz(clazz) // 👈 FIX
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

                weekIndex++;
            }

            List<ScheduleItem> saved = scheduleItemRepository.saveAll(result);
            saved.sort(Comparator.comparingInt(ScheduleItem::getSessionNumber));
            return saved.stream().map(this::toDto).toList();
        }

        // =========================================================================
        // 2. GET
        // =========================================================================
        @Override
        public List<ScheduleItemResponseDTO> getScheduleByCourse(Long courseId) {
            return scheduleItemRepository.findByCourseIdOrderBySessionNumber(courseId)
                    .stream().map(this::toDto).toList();
        }

        // =========================================================================
        // 3. CLEAR
        // =========================================================================
        @Override
        @Transactional
        public void clearScheduleForCourse(Long courseId) {
            scheduleItemRepository.deleteByCourseId(courseId);
        }

        @Override
        @Transactional
        public ScheduleItemResponseDTO updateScheduleItem(Long id, UpdateScheduleItemRequestDTO req) {

            ScheduleItem item = scheduleItemRepository.findById(id)
                    .orElseThrow(() -> new HttpBadRequest("Không tìm thấy scheduleItem id = " + id));

            // -----------------------------
            // 1. UPDATE CLASS (optional)
            // -----------------------------
            if (req.getClassId() != null) {
                Class clazz = classRepository.findById(req.getClassId())
                        .orElseThrow(() -> new HttpBadRequest("Không tìm thấy class id = " + req.getClassId()));
                item.setClazz(clazz);
            }

            // -----------------------------
            // 2. UPDATE PERIOD (optional)
            // -----------------------------
            if (req.getPeriodId() != null) {
                Period period = periodRepository.findById(req.getPeriodId())
                        .orElseThrow(() -> new HttpBadRequest("Không tìm thấy period id = " + req.getPeriodId()));
                item.setPeriod(period);

                // cập nhật startAt / endAt theo Period mới
                if (item.getDate() != null) {
                    item.setStartAt(LocalDateTime.of(item.getDate(), period.getStartTime()));
                    item.setEndAt(LocalDateTime.of(item.getDate(), period.getEndTime()));
                }
            }

            // -----------------------------
            // 3. UPDATE DATE (optional)
            // -----------------------------
            if (req.getDate() != null) {
                item.setDate(req.getDate());

                if (item.getPeriod() != null) {
                    item.setStartAt(LocalDateTime.of(req.getDate(), item.getPeriod().getStartTime()));
                    item.setEndAt(LocalDateTime.of(req.getDate(), item.getPeriod().getEndTime()));
                }
            }

            // -----------------------------
            // 4. UPDATE STATUS (optional)
            // -----------------------------
            if (req.getStatus() != null) {
                item.setStatus(req.getStatus());
            }

            // -----------------------------
            // UPDATE TIMESTAMP
            // -----------------------------
            item.setUpdatedAt(LocalDateTime.now());

            ScheduleItem saved = scheduleItemRepository.save(item);
            return toDto(saved);
        }


        // =========================================================================
        // 5. MANUAL
        // =========================================================================
        @Override
        @Transactional
        public List<ScheduleItemResponseDTO> createManualSchedule(CreateManualScheduleRequestDTO req) {

            Course course = courseRepository.findById(req.getCourseId())
                    .orElseThrow(() -> new HttpBadRequest("Không tìm thấy course id = " + req.getCourseId()));

            Class clazz = classRepository.findById(req.getClassId())
                    .orElseThrow(() -> new HttpBadRequest("Không tìm thấy lớp id = " + req.getClassId()));

            if (course.getStartDate() == null) {
                throw new HttpBadRequest("Khóa học chưa có startDate.");
            }

            int totalSessions = course.getTotalSessions();
            LocalDate startDate = course.getStartDate();

            if (req.getDaysOfWeek() == null || req.getDaysOfWeek().isEmpty())
                throw new HttpBadRequest("daysOfWeek rỗng");

            if (req.getPeriodIds() == null || req.getPeriodIds().isEmpty())
                throw new HttpBadRequest("periodIds rỗng");

            if (req.getDaysOfWeek().size() != req.getPeriodIds().size())
                throw new HttpBadRequest("daysOfWeek và periodIds phải cùng size");

            List<Period> periods = periodRepository.findAllById(req.getPeriodIds());
            if (periods.size() != req.getPeriodIds().size())
                throw new HttpBadRequest("Một hoặc nhiều periodIds không tồn tại");

            scheduleItemRepository.deleteByCourseId(course.getId());

            List<ScheduleItem> result = new ArrayList<>();
            int sessionNumber = 1;

            List<Map.Entry<DayOfWeek, Period>> pairs = new ArrayList<>();
            for (int i = 0; i < req.getDaysOfWeek().size(); i++) {
                pairs.add(Map.entry(DayOfWeek.of(req.getDaysOfWeek().get(i)), periods.get(i)));
            }

            pairs.sort(Comparator.comparingInt(e -> e.getKey().getValue()));

            int week = 0;

            while (sessionNumber <= totalSessions) {

                LocalDate weekStart = startDate.plusWeeks(week)
                        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

                for (var entry : pairs) {
                    if (sessionNumber > totalSessions) break;

                    DayOfWeek dow = entry.getKey();
                    Period p = entry.getValue();

                    LocalDate date = weekStart.with(TemporalAdjusters.nextOrSame(dow));

                    ScheduleItem item = ScheduleItem.builder()
                            .course(course)
                            .clazz(clazz) // 👈 FIX
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

                week++;
            }

            List<ScheduleItem> saved = scheduleItemRepository.saveAll(result);
            saved.sort(Comparator.comparingInt(ScheduleItem::getSessionNumber));
            return saved.stream().map(this::toDto).toList();
        }

        // =========================================================================
        // DTO
        // =========================================================================
        private ScheduleItemResponseDTO toDto(ScheduleItem item) {
            return ScheduleItemResponseDTO.builder()
                    .id(item.getId())
                    .courseId(item.getCourse().getId())
                    .classId(item.getClazz().getId())    // 👈 ĐÃ THÊM
                    .periodId(item.getPeriod().getId())
                    .sessionNumber(item.getSessionNumber())
                    .date(item.getDate())
                    .startAt(item.getStartAt())
                    .endAt(item.getEndAt())
                    .status(item.getStatus())
                    .build();
        }
    }
