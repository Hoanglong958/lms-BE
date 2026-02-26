package com.ra.base_spring_boot.services.course.impl;

import com.ra.base_spring_boot.dto.Roadmap.RoadmapAssignRequest;
import com.ra.base_spring_boot.dto.Roadmap.RoadmapItemResponse;
import com.ra.base_spring_boot.dto.Roadmap.RoadmapResponse;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.*;
import com.ra.base_spring_boot.repository.classroom.IClassRepository;
import com.ra.base_spring_boot.repository.classroom.IScheduleItemContentRepository;
import com.ra.base_spring_boot.repository.classroom.IScheduleItemRepository;
import com.ra.base_spring_boot.repository.course.ICourseRepository;
import com.ra.base_spring_boot.repository.course.ILessonRepository;
import com.ra.base_spring_boot.repository.course.IRoadmapAssignmentRepository;
import com.ra.base_spring_boot.repository.course.ISessionRepository;
import com.ra.base_spring_boot.services.course.IRoadmapService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoadmapServiceImpl implements IRoadmapService {

    private final IRoadmapAssignmentRepository roadmapAssignmentRepository;
    private final ISessionRepository sessionRepository;
    private final ILessonRepository lessonRepository;
    private final IClassRepository classRepository;
    private final ICourseRepository courseRepository;
    private final IScheduleItemRepository scheduleItemRepository;
    private final IScheduleItemContentRepository scheduleItemContentRepository;

    @Override
    @Transactional
    public RoadmapResponse assign(RoadmapAssignRequest req) {
        Long classId = Objects.requireNonNull(req.getClassId(), "classId must not be null");
        Long courseId = Objects.requireNonNull(req.getCourseId(), "courseId must not be null");

        // 1. Gather the target slots (ScheduleItems)
        List<ScheduleItem> allSchedule = scheduleItemRepository
                .findByClassCourse_Course_IdOrderByDateAscSessionNumberAsc(courseId);

        boolean isIncremental = req.getScheduleIds() != null && !req.getScheduleIds().isEmpty();
        List<ScheduleItem> targetSlots;

        if (isIncremental) {
            Set<Long> wantedIds = new HashSet<>(req.getScheduleIds());
            targetSlots = allSchedule.stream()
                    .filter(si -> wantedIds.contains(si.getId()))
                    .collect(Collectors.toList());
            if (targetSlots.isEmpty()) {
                throw new HttpBadRequest("Không tìm thấy các buổi học (Schedules) được chỉ định");
            }
        } else {
            targetSlots = allSchedule;
            if (targetSlots.isEmpty()) {
                throw new HttpBadRequest("Khóa học chưa có thời khóa biểu, không thể gán lộ trình");
            }
        }

        com.ra.base_spring_boot.model.Class clazz = classRepository.findById(classId)
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy lớp với id = " + classId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy khóa học với id = " + courseId));

        // Load existing or create new assignment
        RoadmapAssignment assignment = roadmapAssignmentRepository
                .findByClazz_IdAndCourse_Id(classId, courseId)
                .orElseGet(
                        () -> RoadmapAssignment.builder().clazz(clazz).course(course).items(new ArrayList<>()).build());

        // 2. Clear previous content mapping for the affected slots
        if (!isIncremental) {
            assignment.getItems().clear(); // Clear master roadmap items only on full reset
        }
        // Persist assignment early to obtain id for clearing contents
        assignment = roadmapAssignmentRepository.save(assignment);

        if (isIncremental) {
            // Clear existing schedule-item contents only for the targeted schedule items
            scheduleItemContentRepository.deleteByAssignment_IdAndScheduleItem_IdIn(assignment.getId(),
                    req.getScheduleIds());
        } else {
            // Clear all existing schedule-item contents for this assignment
            scheduleItemContentRepository.deleteByAssignment_Id(assignment.getId());
        }

        // 3. Resolve Units (Sessions & Lessons) provided in this request
        Map<Long, Session> sessionMap = new LinkedHashMap<>();
        Map<Long, Lesson> lessonMap = new LinkedHashMap<>();

        if (req.getSessionIds() != null && !req.getSessionIds().isEmpty()) {
            List<Session> sessions = sessionRepository.findAllById(req.getSessionIds());
            Set<Long> invalid = new HashSet<>(req.getSessionIds());
            for (Session s : sessions) {
                invalid.remove(s.getId());
                if (!Objects.equals(s.getCourse().getId(), courseId)) {
                    throw new HttpBadRequest("Session " + s.getId() + " không thuộc course " + courseId);
                }
                sessionMap.put(s.getId(), s);
            }
            if (!invalid.isEmpty()) {
                throw new HttpBadRequest("Không tìm thấy sessionIds: " + invalid);
            }
        }

        if (req.getLessonIds() != null && !req.getLessonIds().isEmpty()) {
            List<Lesson> lessons = lessonRepository.findAllById(req.getLessonIds());
            Set<Long> invalid = new HashSet<>(req.getLessonIds());
            for (Lesson l : lessons) {
                invalid.remove(l.getId());
                Session s = l.getSession();
                if (s == null || s.getCourse() == null || !Objects.equals(s.getCourse().getId(), courseId)) {
                    throw new HttpBadRequest("Lesson " + l.getId() + " không thuộc course " + courseId);
                }
                // optional: if request provided sessionIds, ensure lesson's session is within
                // those
                if (req.getSessionIds() != null && !req.getSessionIds().isEmpty()
                        && !sessionMap.containsKey(s.getId())) {
                    throw new HttpBadRequest("Lesson " + l.getId() + " không thuộc các session đã chọn");
                }
                lessonMap.put(l.getId(), l);
                sessionMap.putIfAbsent(s.getId(), s);
            }
            if (!invalid.isEmpty()) {
                throw new HttpBadRequest("Không tìm thấy lessonIds: " + invalid);
            }
        }

        // Auto-assign all sessions and lessons when no IDs are provided and it's not an
        // incremental update
        if (!isIncremental && sessionMap.isEmpty() && lessonMap.isEmpty()) {
            List<Session> allSessions = sessionRepository.findByCourse_IdOrderByOrderIndexAsc(courseId);
            for (Session s : allSessions) {
                sessionMap.put(s.getId(), s);
                List<Lesson> lessonsOfSession = lessonRepository.findBySession_Id(s.getId());
                for (Lesson l : lessonsOfSession) {
                    lessonMap.put(l.getId(), l);
                }
            }
        }

        // 4. Update the Master Roadmap (RoadmapItems)
        // In incremental mode, we add missing items to the master list if they are part
        // of the current request.
        // In non-incremental mode, assignment.getItems() was cleared, so all will be
        // new.
        int currentMaxOrder = assignment.getItems().stream()
                .mapToInt(i -> Optional.ofNullable(i.getOrderIndex()).orElse(0))
                .max().orElse(0);

        List<RoadmapItem> newItemsToMap = new ArrayList<>(); // Items from this request that need to be mapped to slots
        List<Session> orderedSessions = sessionMap.values().stream()
                .sorted(Comparator.comparing(s -> s.getOrderIndex() != null ? s.getOrderIndex() : 0)).toList();

        for (Session s : orderedSessions) {
            List<Lesson> lessonsForThisSession = lessonMap.values().stream()
                    .filter(l -> l.getSession().getId().equals(s.getId()))
                    .sorted(Comparator.comparing(l -> l.getOrderIndex() != null ? l.getOrderIndex() : 0)).toList();

            if (lessonsForThisSession.isEmpty()) {
                // Case: Session has no selected lessons -> add the session as a unit
                Optional<RoadmapItem> existingItem = assignment.getItems().stream()
                        .filter(i -> i.getSession() != null && i.getLesson() == null
                                && i.getSession().getId().equals(s.getId()))
                        .findFirst();

                if (existingItem.isEmpty()) {
                    RoadmapItem item = RoadmapItem.builder().assignment(assignment).session(s)
                            .orderIndex(++currentMaxOrder).build();
                    assignment.getItems().add(item);
                    newItemsToMap.add(item);
                } else {
                    newItemsToMap.add(existingItem.get());
                }
            } else {
                // Case: Session has selected lessons -> add only the lessons to represent the
                // session's content
                for (Lesson l : lessonsForThisSession) {
                    Optional<RoadmapItem> existingItem = assignment.getItems().stream()
                            .filter(i -> i.getLesson() != null && i.getLesson().getId().equals(l.getId()))
                            .findFirst();

                    if (existingItem.isEmpty()) {
                        RoadmapItem item = RoadmapItem.builder().assignment(assignment).session(s).lesson(l)
                                .orderIndex(++currentMaxOrder).build();
                        assignment.getItems().add(item);
                        newItemsToMap.add(item);
                    } else {
                        newItemsToMap.add(existingItem.get());
                    }
                }
            }
        }

        RoadmapAssignment saved = roadmapAssignmentRepository.save(assignment);

        // 5. Map the units provided in THIS request to the targeted slots
        List<ScheduleItemContent> contents = new ArrayList<>();
        int slotIdx = 0;
        int contentOrder = 1;

        // Spread the learning units (items from this request) across available target
        // slots
        for (RoadmapItem ri : newItemsToMap) {
            ScheduleItem si = targetSlots.get(slotIdx % targetSlots.size());
            slotIdx++;

            contents.add(ScheduleItemContent.builder()
                    .assignment(saved)
                    .scheduleItem(si)
                    .session(ri.getSession())
                    .lesson(ri.getLesson())
                    .orderIndex(contentOrder++)
                    .build());
        }

        if (!contents.isEmpty()) {
            scheduleItemContentRepository.saveAll(contents);
        }

        return toResponse(saved);
    }

    @Override
    @Transactional
    public java.util.List<RoadmapResponse> assignBulk(java.util.List<RoadmapAssignRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return new ArrayList<>();
        }
        java.util.List<RoadmapResponse> responses = new ArrayList<>();
        for (RoadmapAssignRequest req : requests) {
            responses.add(this.assign(req));
        }
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public RoadmapResponse get(Long classId, Long courseId) {
        RoadmapAssignment assignment = roadmapAssignmentRepository
                .findByClazz_IdAndCourse_Id(classId, courseId)
                .orElseThrow(() -> new HttpBadRequest(
                        "Không tìm thấy lộ trình cho classId=" + classId + ", courseId=" + courseId));
        return toResponse(assignment);
    }

    @Override
    @Transactional
    public void clear(Long classId, Long courseId) {
        roadmapAssignmentRepository.deleteByClazz_IdAndCourse_Id(classId, courseId);
    }

    private RoadmapResponse toResponse(RoadmapAssignment assignment) {
        List<RoadmapItemResponse> items = assignment.getItems().stream()
                .sorted(Comparator.comparing(i -> Optional.ofNullable(i.getOrderIndex()).orElse(0)))
                .map(i -> RoadmapItemResponse.builder()
                        .id(i.getId())
                        .orderIndex(i.getOrderIndex())
                        .sessionId(i.getSession() != null ? i.getSession().getId() : null)
                        .sessionTitle(i.getSession() != null ? i.getSession().getTitle() : null)
                        .lessonId(i.getLesson() != null ? i.getLesson().getId() : null)
                        .lessonTitle(i.getLesson() != null ? i.getLesson().getTitle() : null)
                        .build())
                .collect(Collectors.toList());
        return RoadmapResponse.builder()
                .classId(assignment.getClazz().getId())
                .courseId(assignment.getCourse().getId())
                .items(items)
                .build();
    }
}
