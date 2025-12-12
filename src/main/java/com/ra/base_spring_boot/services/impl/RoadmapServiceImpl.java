package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.Roadmap.RoadmapAssignRequest;
import com.ra.base_spring_boot.dto.Roadmap.RoadmapItemResponse;
import com.ra.base_spring_boot.dto.Roadmap.RoadmapResponse;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.*;
import com.ra.base_spring_boot.repository.*;
import com.ra.base_spring_boot.services.IRoadmapService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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

        // Load schedule for the target class + optional filter by periods
        List<ScheduleItem> schedule = scheduleItemRepository.findByCourse_IdAndClazz_IdOrderBySessionNumber(courseId, classId);
        if (req.getPeriodIds() != null && !req.getPeriodIds().isEmpty()) {
            Set<Long> wanted = new HashSet<>(req.getPeriodIds());
            schedule = schedule.stream()
                    .filter(si -> si.getPeriod() != null && wanted.contains(si.getPeriod().getId()))
                    .toList();
        }
        if (schedule.isEmpty()) {
            throw new HttpBadRequest("Khóa học chưa có thời khóa biểu, không thể gán lộ trình");
        }

        com.ra.base_spring_boot.model.Class clazz = classRepository.findById(classId)
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy lớp với id = " + classId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy khóa học với id = " + courseId));

        // Load existing or create new assignment
        RoadmapAssignment assignment = roadmapAssignmentRepository
                .findByClazz_IdAndCourse_Id(classId, courseId)
                .orElseGet(() -> RoadmapAssignment.builder().clazz(clazz).course(course).items(new ArrayList<>()).build());

        // Clear current items
        assignment.getItems().clear();
        // Persist assignment early to obtain id for clearing contents
        assignment = roadmapAssignmentRepository.save(assignment);
        // Clear existing schedule-item contents for this assignment
        scheduleItemContentRepository.deleteByAssignment_Id(assignment.getId());

        // Validate sessions belong to course
        Map<Long, Session> sessionMap = new HashMap<>();
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

        // Validate lessons belong to the above sessions (if provided), otherwise to course's sessions
        Map<Long, Lesson> lessonMap = new HashMap<>();
        if (req.getLessonIds() != null && !req.getLessonIds().isEmpty()) {
            List<Lesson> lessons = lessonRepository.findAllById(req.getLessonIds());
            Set<Long> invalid = new HashSet<>(req.getLessonIds());
            for (Lesson l : lessons) {
                invalid.remove(l.getId());
                Session s = l.getSession();
                if (s == null || s.getCourse() == null || !Objects.equals(s.getCourse().getId(), courseId)) {
                    throw new HttpBadRequest("Lesson " + l.getId() + " không thuộc course " + courseId);
                }
                // optional: if request provided sessionIds, ensure lesson's session is within those
                if (req.getSessionIds() != null && !req.getSessionIds().isEmpty() && !sessionMap.containsKey(s.getId())) {
                    throw new HttpBadRequest("Lesson " + l.getId() + " không thuộc các session đã chọn");
                }
                lessonMap.put(l.getId(), l);
                sessionMap.putIfAbsent(s.getId(), s);
            }
            if (!invalid.isEmpty()) {
                throw new HttpBadRequest("Không tìm thấy lessonIds: " + invalid);
            }
        }

        // Auto-assign all sessions and lessons when no IDs are provided
        boolean noSessionsProvided = req.getSessionIds() == null || req.getSessionIds().isEmpty();
        boolean noLessonsProvided = req.getLessonIds() == null || req.getLessonIds().isEmpty();
        if (noSessionsProvided && noLessonsProvided) {
            // Load all sessions of the course in order
            List<Session> allSessions = sessionRepository.findByCourse_IdOrderByOrderIndexAsc(courseId);
            for (Session s : allSessions) {
                sessionMap.putIfAbsent(s.getId(), s);
                // Load all lessons for each session
                List<Lesson> lessonsOfSession = lessonRepository.findBySession_Id(s.getId());
                for (Lesson l : lessonsOfSession) {
                    lessonMap.putIfAbsent(l.getId(), l);
                }
            }
        }

        // Build items with stable order: sessions first by orderIndex, then lessons by their session/orderIndex
        int order = 1;
        // Add sessions
        List<Session> orderedSessions = sessionMap.values().stream()
                .sorted(Comparator.comparing(Session::getOrderIndex))
                .toList();
        for (Session s : orderedSessions) {
            RoadmapItem item = RoadmapItem.builder()
                    .assignment(assignment)
                    .session(s)
                    .orderIndex(order++)
                    .build();
            assignment.getItems().add(item);
        }
        // Add lessons
        List<Lesson> orderedLessons = lessonMap.values().stream()
                .sorted(Comparator.comparing((Lesson l) -> l.getSession().getOrderIndex())
                        .thenComparing(Lesson::getOrderIndex))
                .toList();
        for (Lesson l : orderedLessons) {
            RoadmapItem item = RoadmapItem.builder()
                    .assignment(assignment)
                    .session(l.getSession())
                    .lesson(l)
                    .orderIndex(order++)
                    .build();
            assignment.getItems().add(item);
        }

        RoadmapAssignment saved = roadmapAssignmentRepository.save(assignment);

        // Map ordered sessions and lessons to filtered schedule items via round-robin
        List<ScheduleItem> targetSchedule = schedule;
        if (targetSchedule.isEmpty()) {
            throw new HttpBadRequest("Không tìm thấy buổi học phù hợp với periodIds được chỉ định");
        }

        List<ScheduleItemContent> contents = new ArrayList<>();
        int idx = 0;
        int contentOrder = 1;
        List<Object> units = new ArrayList<>();
        units.addAll(orderedSessions);
        units.addAll(orderedLessons);

        for (Object u : units) {
            ScheduleItem si = targetSchedule.get(idx % targetSchedule.size());
            idx++;

            if (u instanceof Session s) {
                contents.add(ScheduleItemContent.builder()
                        .assignment(saved)
                        .scheduleItem(si)
                        .session(s)
                        .orderIndex(contentOrder++)
                        .build());
            } else if (u instanceof Lesson l) {
                contents.add(ScheduleItemContent.builder()
                        .assignment(saved)
                        .scheduleItem(si)
                        .session(l.getSession())
                        .lesson(l)
                        .orderIndex(contentOrder++)
                        .build());
            }
        }

        if (!contents.isEmpty()) {
            scheduleItemContentRepository.saveAll(contents);
        }

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RoadmapResponse get(Long classId, Long courseId) {
        RoadmapAssignment assignment = roadmapAssignmentRepository
                .findByClazz_IdAndCourse_Id(classId, courseId)
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy lộ trình cho classId=" + classId + ", courseId=" + courseId));
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
