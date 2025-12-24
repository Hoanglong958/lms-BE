package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.UserProgress.*;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.*;
import com.ra.base_spring_boot.model.constants.LessonProgressStatus;
import com.ra.base_spring_boot.model.constants.LessonType;
import com.ra.base_spring_boot.model.constants.UserCourseStatus;
import com.ra.base_spring_boot.repository.*;
import com.ra.base_spring_boot.services.IUserProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserProgressServiceImpl implements IUserProgressService {

    private final IUserRepository userRepository;
    private final ICourseRepository courseRepository;
    private final ISessionRepository sessionRepository;
    private final ILessonRepository lessonRepository;
    private final IUserCourseProgressRepository userCourseProgressRepository;
    private final IUserSessionProgressRepository userSessionProgressRepository;
    private final IUserLessonProgressRepository userLessonProgressRepository;
    private final IRoadmapAssignmentRepository roadmapAssignmentRepository;
    private final IUserRoadmapProgressRepository userRoadmapProgressRepository;
    private final IClassStudentRepository classStudentRepository;

    // ===== Khóa học =====
    @Override
    @Transactional
    public UserCourseProgressResponseDTO upsertCourseProgress(UserCourseProgressRequestDTO dto) {
        User user = requireUser(dto.getUserId());
        Course course = requireCourse(dto.getCourseId());

        UserCourseProgress progress = userCourseProgressRepository
                .findByUserIdAndCourseId(user.getId(), course.getId())
                .orElseGet(() -> UserCourseProgress.builder()
                        .user(user)
                        .course(course)
                        .build());

        if (dto.getProgressPercent() != null) {
            progress.setProgressPercent(normalizePercent(dto.getProgressPercent()));
        }
        if (dto.getCompletedSessions() != null) {
            progress.setCompletedSessions(Math.max(0, dto.getCompletedSessions()));
        }
        if (dto.getTotalSessions() != null) {
            progress.setTotalSessions(Math.max(0, dto.getTotalSessions()));
        }
        if (dto.getStatus() != null) {
            progress.setStatus(parseCourseStatus(dto.getStatus()));
        }
        progress.setLastAccessedAt(LocalDateTime.now());

        userCourseProgressRepository.save(progress);
        return toCourseDto(progress);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserCourseProgressResponseDTO> getCourseProgressByUser(Long userId) {
        return userCourseProgressRepository.findByUserId(userId)
                .stream()
                .map(this::toCourseDto)
                .toList();
    }

    // ===== Session =====
    @Override
    @Transactional
    public UserSessionProgressResponseDTO upsertSessionProgress(UserSessionProgressRequestDTO dto) {
        User user = requireUser(dto.getUserId());
        Session session = requireSession(dto.getSessionId());
        Course course = requireCourse(dto.getCourseId());

        if (!session.getCourse().getId().equals(course.getId())) {
            throw new HttpBadRequest("Session không thuộc khóa học đã chọn");
        }

        UserSessionProgress progress = userSessionProgressRepository
                .findByUserIdAndSessionId(user.getId(), session.getId())
                .orElseGet(() -> UserSessionProgress.builder()
                        .user(user)
                        .session(session)
                        .course(course)
                        .build());

        if (dto.getStatus() != null) {
            progress.setStatus(parseProgressStatus(dto.getStatus()));
        }
        if (dto.getProgressPercent() != null) {
            progress.setProgressPercent(normalizePercent(dto.getProgressPercent()));
        }
        if (progress.getStatus() == LessonProgressStatus.IN_PROGRESS && progress.getStartedAt() == null) {
            progress.setStartedAt(LocalDateTime.now());
        }
        if (progress.getStatus() == LessonProgressStatus.COMPLETED && progress.getCompletedAt() == null) {
            progress.setCompletedAt(LocalDateTime.now());
        }

        userSessionProgressRepository.save(progress);
        List<ClassStudent> enrollments = classStudentRepository.findByStudent_Id(user.getId());
        for (ClassStudent cs : enrollments) {
            Long classId = cs.getClassroom().getId();
            Long courseId = course.getId();
            RoadmapAssignment ra = roadmapAssignmentRepository
                    .findByClazz_IdAndCourse_Id(classId, courseId)
                    .orElse(null);
            if (ra == null)
                continue;

            UserRoadmapProgress rp = userRoadmapProgressRepository
                    .findByUserIdAndAssignmentId(user.getId(), ra.getId())
                    .orElseGet(() -> UserRoadmapProgress.builder()
                            .user(user)
                            .assignment(ra)
                            .totalItems(ra.getItems() != null ? ra.getItems().size() : 0)
                            .build());

            if (ra.getItems() != null) {
                ra.getItems().stream()
                        .filter(i -> i.getSession() != null && Objects.equals(i.getSession().getId(), session.getId()))
                        .findFirst()
                        .ifPresent(rp::setCurrentItem);
            }

            if (progress.getStatus() == LessonProgressStatus.IN_PROGRESS && rp.getStartedAt() == null) {
                rp.setStartedAt(LocalDateTime.now());
                rp.setStatus(LessonProgressStatus.IN_PROGRESS);
            }
            int total = ra.getItems() != null ? ra.getItems().size() : 0;
            int completed = computeCompletedRoadmapItems(user, ra);
            rp.setTotalItems(total);
            rp.setCompletedItems(completed);
            if (total > 0 && completed >= total) {
                rp.setStatus(LessonProgressStatus.COMPLETED);
                if (rp.getCompletedAt() == null)
                    rp.setCompletedAt(LocalDateTime.now());
            } else if (completed > 0 && rp.getStatus() == LessonProgressStatus.NOT_STARTED) {
                rp.setStatus(LessonProgressStatus.IN_PROGRESS);
            }

            userRoadmapProgressRepository.save(rp);
            break;
        }
        return toSessionDto(progress);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSessionProgressResponseDTO> getSessionProgressByUserAndCourse(Long userId, Long courseId) {
        return userSessionProgressRepository.findByUserIdAndCourseId(userId, courseId)
                .stream()
                .map(this::toSessionDto)
                .toList();
    }

    // ===== Lesson =====
    @Override
    @Transactional
    public UserLessonProgressResponseDTO upsertLessonProgress(UserLessonProgressRequestDTO dto) {
        User user = requireUser(dto.getUserId());
        Lesson lesson = requireLesson(dto.getLessonId());
        Session session = requireSession(dto.getSessionId());
        Course course = requireCourse(dto.getCourseId());

        if (!lesson.getSession().getId().equals(session.getId())) {
            throw new HttpBadRequest("Lesson không thuộc session đã chọn");
        }
        if (!session.getCourse().getId().equals(course.getId())) {
            throw new HttpBadRequest("Session không thuộc khóa học đã chọn");
        }

        UserLessonProgress progress = userLessonProgressRepository
                .findByUserIdAndLessonId(user.getId(), lesson.getId())
                .orElseGet(() -> UserLessonProgress.builder()
                        .user(user)
                        .lesson(lesson)
                        .session(session)
                        .course(course)
                        .type(lesson.getType())
                        .build());

        if (dto.getType() != null) {
            progress.setType(parseLessonType(dto.getType()));
        }
        if (dto.getStatus() != null) {
            progress.setStatus(parseProgressStatus(dto.getStatus()));
        }
        if (dto.getProgressPercent() != null) {
            progress.setProgressPercent(normalizePercent(dto.getProgressPercent()));
        }

        // Xử lý video progress nếu là bài học video
        if (progress.getType() == LessonType.VIDEO) {
            if (dto.getLastPosition() != null) {
                progress.setLastPosition(dto.getLastPosition());
            }
            if (dto.getVideoDuration() != null && dto.getVideoDuration() > 0) {
                progress.setVideoDuration(dto.getVideoDuration());
                // Tự động tính % nếu có duration và lastPosition
                if (progress.getLastPosition() != null) {
                    double percent = (progress.getLastPosition().doubleValue()
                            / progress.getVideoDuration().doubleValue()) * 100;
                    progress.setProgressPercent(normalizePercent(BigDecimal.valueOf(percent)));
                }
            }
            // Nếu % >= 95 thì tự động hoàn thành (tùy chọn, ở đây ta để user gửi COMPLETED
            // hoặc dựa trên % nếu muốn)
            if (progress.getProgressPercent().compareTo(BigDecimal.valueOf(95)) >= 0) {
                progress.setStatus(LessonProgressStatus.COMPLETED);
            }
        }

        if (progress.getStatus() == LessonProgressStatus.IN_PROGRESS && progress.getStartedAt() == null) {
            progress.setStartedAt(LocalDateTime.now());
        }
        if (progress.getStatus() == LessonProgressStatus.COMPLETED && progress.getCompletedAt() == null) {
            progress.setCompletedAt(LocalDateTime.now());
        }

        userLessonProgressRepository.save(progress);
        userLessonProgressRepository.flush(); // 🔥 Quan trọng: Đảm bảo dữ liệu được đẩy xuống DB để các câu query sau
                                              // thấy được

        // Đồng bộ lên Session -> Course -> Roadmap
        trySyncSessionProgress(user, session);
        trySyncRoadmapProgress(user, course, lesson, progress.getStatus());

        return toLessonDto(progress);
    }

    private void trySyncSessionProgress(User user, Session session) {
        Course course = session.getCourse();
        UserSessionProgress sp = userSessionProgressRepository
                .findByUserIdAndSessionId(user.getId(), session.getId())
                .orElseGet(() -> UserSessionProgress.builder()
                        .user(user)
                        .session(session)
                        .course(course)
                        .status(LessonProgressStatus.NOT_STARTED)
                        .progressPercent(BigDecimal.ZERO)
                        .build());

        List<Lesson> lessons = session.getLessons();
        if (lessons == null || lessons.isEmpty()) {
            return;
        }

        // Lấy tất cả progress bài học của user trong session này
        List<UserLessonProgress> lessonProgresses = userLessonProgressRepository
                .findByUserIdAndSessionId(user.getId(), session.getId());

        long completedCount = lessonProgresses.stream()
                .filter(p -> p.getStatus() == LessonProgressStatus.COMPLETED)
                .count();

        double percent = (double) completedCount / lessons.size() * 100;
        sp.setProgressPercent(normalizePercent(BigDecimal.valueOf(percent)));

        if (completedCount >= lessons.size()) {
            sp.setStatus(LessonProgressStatus.COMPLETED);
            if (sp.getCompletedAt() == null)
                sp.setCompletedAt(LocalDateTime.now());
        } else if (completedCount > 0 || sp.getProgressPercent().compareTo(BigDecimal.ZERO) > 0) {
            sp.setStatus(LessonProgressStatus.IN_PROGRESS);
            if (sp.getStartedAt() == null)
                sp.setStartedAt(LocalDateTime.now());
        }

        userSessionProgressRepository.save(sp);
        userSessionProgressRepository.flush(); // 🔥 Flush để khóa học thấy được trạng thái mới
        trySyncCourseProgress(user, course);
    }

    private void trySyncCourseProgress(User user, Course course) {
        UserCourseProgress cp = userCourseProgressRepository
                .findByUserIdAndCourseId(user.getId(), course.getId())
                .orElseGet(() -> UserCourseProgress.builder()
                        .user(user)
                        .course(course)
                        .status(UserCourseStatus.ENROLLED)
                        .progressPercent(BigDecimal.ZERO)
                        .build());

        List<Session> sessions = course.getSessions();
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        // Lấy tất cả progress của từng session trong khóa học
        List<UserSessionProgress> sessionProgresses = userSessionProgressRepository
                .findByUserIdAndCourseId(user.getId(), course.getId());

        // Tạo map để tra cứu nhanh theo sessionId
        java.util.Map<Long, UserSessionProgress> progressMap = sessionProgresses.stream()
                .collect(java.util.stream.Collectors.toMap(p -> p.getSession().getId(), p -> p));

        BigDecimal totalPercent = BigDecimal.ZERO;
        int completedCount = 0;

        for (Session s : sessions) {
            UserSessionProgress sp = progressMap.get(s.getId());
            if (sp != null) {
                totalPercent = totalPercent.add(sp.getProgressPercent());
                if (sp.getStatus() == LessonProgressStatus.COMPLETED) {
                    completedCount++;
                }
            }
        }

        int totalSessions = sessions.size();
        cp.setTotalSessions(totalSessions);
        cp.setCompletedSessions(completedCount);

        // Tiến độ khóa học = Trung bình cộng tiến độ của các chương
        BigDecimal averagePercent = totalPercent.divide(BigDecimal.valueOf(totalSessions), 2,
                java.math.RoundingMode.HALF_UP);
        cp.setProgressPercent(normalizePercent(averagePercent));

        // Cập nhật trạng thái
        if (completedCount >= totalSessions) {
            cp.setStatus(UserCourseStatus.COMPLETED);
        } else if (cp.getProgressPercent().compareTo(BigDecimal.ZERO) > 0 || completedCount > 0) {
            cp.setStatus(UserCourseStatus.IN_PROGRESS);
        }

        cp.setLastAccessedAt(LocalDateTime.now());
        userCourseProgressRepository.save(cp);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserLessonProgressResponseDTO> getLessonProgressByUserAndCourse(Long userId, Long courseId) {
        return userLessonProgressRepository.findByUserIdAndCourseId(userId, courseId)
                .stream()
                .map(this::toLessonDto)
                .toList();
    }

    // ===== Helpers =====
    private User requireUser(Long userId) {
        return userRepository.findById(java.util.Objects.requireNonNull(userId, "userId must not be null"))
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy user với id = " + userId));
    }

    private Course requireCourse(Long courseId) {
        return courseRepository.findById(java.util.Objects.requireNonNull(courseId, "courseId must not be null"))
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy khóa học với id = " + courseId));
    }

    private Session requireSession(Long sessionId) {
        return sessionRepository.findById(java.util.Objects.requireNonNull(sessionId, "sessionId must not be null"))
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy session với id = " + sessionId));
    }

    private Lesson requireLesson(Long lessonId) {
        return lessonRepository.findById(java.util.Objects.requireNonNull(lessonId, "lessonId must not be null"))
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy lesson với id = " + lessonId));
    }

    // ===== Roadmap Progress =====
    @Override
    @Transactional
    public UserRoadmapProgressResponseDTO upsertRoadmapProgress(UserRoadmapProgressRequestDTO dto) {
        User user = requireUser(dto.getUserId());
        Long roadmapId = Objects.requireNonNull(dto.getRoadmapId(), "roadmapId must not be null");
        RoadmapAssignment assignment = roadmapAssignmentRepository.findById(roadmapId)
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy roadmap với id = " + roadmapId));

        if (!classStudentRepository.existsByClassroomIdAndStudentId(assignment.getClazz().getId(), user.getId())) {
            throw new HttpBadRequest("User không thuộc lớp của roadmap này");
        }

        UserRoadmapProgress rp = userRoadmapProgressRepository
                .findByUserIdAndAssignmentId(user.getId(), assignment.getId())
                .orElseGet(() -> UserRoadmapProgress.builder()
                        .user(user)
                        .assignment(assignment)
                        .totalItems(assignment.getItems() != null ? assignment.getItems().size() : 0)
                        .build());

        if (dto.getStatus() != null) {
            rp.setStatus(parseProgressStatus(dto.getStatus()));
            if (rp.getStatus() == LessonProgressStatus.IN_PROGRESS && rp.getStartedAt() == null) {
                rp.setStartedAt(LocalDateTime.now());
            }
            if (rp.getStatus() == LessonProgressStatus.COMPLETED && rp.getCompletedAt() == null) {
                rp.setCompletedAt(LocalDateTime.now());
            }
        }

        if (dto.getCurrentItemId() != null && assignment.getItems() != null) {
            RoadmapItem current = assignment.getItems().stream()
                    .filter(i -> Objects.equals(i.getId(), dto.getCurrentItemId()))
                    .findFirst()
                    .orElseThrow(() -> new HttpBadRequest("currentItemId không thuộc roadmap"));
            rp.setCurrentItem(current);
        }

        int total = assignment.getItems() != null ? assignment.getItems().size() : 0;
        int completed = computeCompletedRoadmapItems(user, assignment);
        rp.setTotalItems(total);
        rp.setCompletedItems(completed);
        if (total > 0 && completed >= total) {
            rp.setStatus(LessonProgressStatus.COMPLETED);
            if (rp.getCompletedAt() == null)
                rp.setCompletedAt(LocalDateTime.now());
        } else if (completed > 0 && rp.getStatus() == LessonProgressStatus.NOT_STARTED) {
            rp.setStatus(LessonProgressStatus.IN_PROGRESS);
            if (rp.getStartedAt() == null)
                rp.setStartedAt(LocalDateTime.now());
        }

        userRoadmapProgressRepository.save(rp);
        return toRoadmapDto(rp);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserRoadmapProgressResponseDTO> getRoadmapProgressByUser(Long userId) {
        return userRoadmapProgressRepository.findByUserId(Objects.requireNonNull(userId, "userId must not be null"))
                .stream().map(this::toRoadmapDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserRoadmapProgressResponseDTO getRoadmapProgressByUserAndRoadmap(Long userId, Long roadmapId) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(roadmapId, "roadmapId must not be null");
        RoadmapAssignment assignment = roadmapAssignmentRepository.findById(roadmapId)
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy roadmap với id = " + roadmapId));
        User user = requireUser(userId);

        UserRoadmapProgress rp = userRoadmapProgressRepository
                .findByUserIdAndAssignmentId(userId, assignment.getId())
                .orElseGet(() -> UserRoadmapProgress.builder()
                        .user(user)
                        .assignment(assignment)
                        .totalItems(assignment.getItems() != null ? assignment.getItems().size() : 0)
                        .build());

        int total = assignment.getItems() != null ? assignment.getItems().size() : 0;
        int completed = computeCompletedRoadmapItems(user, assignment);
        rp.setTotalItems(total);
        rp.setCompletedItems(completed);
        if (total > 0 && completed >= total) {
            rp.setStatus(LessonProgressStatus.COMPLETED);
            if (rp.getCompletedAt() == null)
                rp.setCompletedAt(LocalDateTime.now());
        } else if (completed > 0 && rp.getStatus() == LessonProgressStatus.NOT_STARTED) {
            rp.setStatus(LessonProgressStatus.IN_PROGRESS);
        }

        return toRoadmapDto(rp);
    }

    private void trySyncRoadmapProgress(User user, Course course, Lesson lesson, LessonProgressStatus status) {
        // Tìm roadmap assignment phù hợp: theo các lớp mà user thuộc và có assignment
        // cho course này
        List<ClassStudent> enrollments = classStudentRepository.findByStudent_Id(user.getId());
        for (ClassStudent cs : enrollments) {
            Long classId = cs.getClassroom().getId();
            Long courseId = course.getId();
            RoadmapAssignment ra = roadmapAssignmentRepository
                    .findByClazz_IdAndCourse_Id(classId, courseId)
                    .orElse(null);
            if (ra == null)
                continue;

            // Lấy/khởi tạo bản ghi roadmap progress
            UserRoadmapProgress rp = userRoadmapProgressRepository
                    .findByUserIdAndAssignmentId(user.getId(), ra.getId())
                    .orElseGet(() -> UserRoadmapProgress.builder()
                            .user(user)
                            .assignment(ra)
                            .totalItems(ra.getItems() != null ? ra.getItems().size() : 0)
                            .build());

            // Cập nhật current item theo lesson tương ứng (nếu có trong roadmap)
            if (ra.getItems() != null) {
                ra.getItems().stream()
                        .filter(i -> i.getLesson() != null && Objects.equals(i.getLesson().getId(), lesson.getId()))
                        .findFirst()
                        .ifPresent(rp::setCurrentItem);
            }

            if (status == LessonProgressStatus.IN_PROGRESS && rp.getStartedAt() == null) {
                rp.setStartedAt(LocalDateTime.now());
                rp.setStatus(LessonProgressStatus.IN_PROGRESS);
            }
            int total = ra.getItems() != null ? ra.getItems().size() : 0;
            int completed = computeCompletedRoadmapItems(user, ra);
            rp.setTotalItems(total);
            rp.setCompletedItems(completed);
            if (total > 0 && completed >= total) {
                rp.setStatus(LessonProgressStatus.COMPLETED);
                if (rp.getCompletedAt() == null)
                    rp.setCompletedAt(LocalDateTime.now());
            } else if (completed > 0 && rp.getStatus() == LessonProgressStatus.NOT_STARTED) {
                rp.setStatus(LessonProgressStatus.IN_PROGRESS);
            }

            userRoadmapProgressRepository.save(rp);
            // Đồng bộ 1 assignment là đủ
            break;
        }
    }

    private int computeCompletedRoadmapItems(User user, RoadmapAssignment ra) {
        if (ra.getItems() == null || ra.getItems().isEmpty())
            return 0;

        // Lấy tất cả progress của user cho khóa học này để check nhanh
        List<UserLessonProgress> lessonProgs = userLessonProgressRepository.findByUserIdAndCourseId(user.getId(),
                ra.getCourse().getId());
        List<UserSessionProgress> sessionProgs = userSessionProgressRepository.findByUserIdAndCourseId(user.getId(),
                ra.getCourse().getId());

        java.util.Set<Long> completedLessonIds = lessonProgs.stream()
                .filter(p -> p.getStatus() == LessonProgressStatus.COMPLETED)
                .map(p -> p.getLesson().getId())
                .collect(java.util.stream.Collectors.toSet());

        java.util.Set<Long> completedSessionIds = sessionProgs.stream()
                .filter(p -> p.getStatus() == LessonProgressStatus.COMPLETED)
                .map(p -> p.getSession().getId())
                .collect(java.util.stream.Collectors.toSet());

        int done = 0;
        for (RoadmapItem item : ra.getItems()) {
            if (item.getLesson() != null) {
                if (completedLessonIds.contains(item.getLesson().getId())) {
                    done++;
                }
            } else if (item.getSession() != null) {
                if (completedSessionIds.contains(item.getSession().getId())) {
                    done++;
                }
            }
        }
        return done;
    }

    private BigDecimal normalizePercent(BigDecimal value) {
        if (value == null)
            return BigDecimal.ZERO;
        if (value.compareTo(BigDecimal.ZERO) < 0)
            return BigDecimal.ZERO;
        if (value.compareTo(BigDecimal.valueOf(100)) > 0)
            return BigDecimal.valueOf(100);
        return value;
    }

    private UserCourseStatus parseCourseStatus(String raw) {
        try {
            return UserCourseStatus.valueOf(raw.trim().toUpperCase());
        } catch (Exception e) {
            throw new HttpBadRequest("Trạng thái khóa học không hợp lệ (ENROLLED/IN_PROGRESS/COMPLETED)");
        }
    }

    private LessonProgressStatus parseProgressStatus(String raw) {
        try {
            return LessonProgressStatus.valueOf(raw.trim().toUpperCase());
        } catch (Exception e) {
            throw new HttpBadRequest("Trạng thái tiến độ không hợp lệ (NOT_STARTED/IN_PROGRESS/COMPLETED)");
        }
    }

    private LessonType parseLessonType(String raw) {
        try {
            return LessonType.valueOf(raw.trim().toUpperCase());
        } catch (Exception e) {
            throw new HttpBadRequest("Loại bài học không hợp lệ (VIDEO/QUIZ/DOCUMENT)");
        }
    }

    private UserCourseProgressResponseDTO toCourseDto(UserCourseProgress progress) {
        return UserCourseProgressResponseDTO.builder()
                .id(progress.getId())
                .userId(progress.getUser().getId())
                .userName(progress.getUser().getFullName())
                .courseId(progress.getCourse().getId())
                .courseTitle(progress.getCourse().getTitle())
                .enrolledAt(progress.getEnrolledAt())
                .progressPercent(progress.getProgressPercent())
                .completedSessions(progress.getCompletedSessions())
                .totalSessions(progress.getTotalSessions())
                .status(progress.getStatus().name())
                .lastAccessedAt(progress.getLastAccessedAt())
                .build();
    }

    private UserSessionProgressResponseDTO toSessionDto(UserSessionProgress progress) {
        return UserSessionProgressResponseDTO.builder()
                .id(progress.getId())
                .userId(progress.getUser().getId())
                .userName(progress.getUser().getFullName())
                .sessionId(progress.getSession().getId())
                .sessionTitle(progress.getSession().getTitle())
                .courseId(progress.getCourse().getId())
                .courseTitle(progress.getCourse().getTitle())
                .status(progress.getStatus().name())
                .progressPercent(progress.getProgressPercent())
                .startedAt(progress.getStartedAt())
                .completedAt(progress.getCompletedAt())
                .build();
    }

    private UserLessonProgressResponseDTO toLessonDto(UserLessonProgress progress) {
        return UserLessonProgressResponseDTO.builder()
                .id(progress.getId())
                .userId(progress.getUser().getId())
                .userName(progress.getUser().getFullName())
                .lessonId(progress.getLesson().getId())
                .lessonTitle(progress.getLesson().getTitle())
                .type(progress.getType().name().toLowerCase())
                .sessionId(progress.getSession().getId())
                .sessionTitle(progress.getSession().getTitle())
                .courseId(progress.getCourse().getId())
                .courseTitle(progress.getCourse().getTitle())
                .status(progress.getStatus().name())
                .progressPercent(progress.getProgressPercent())
                .lastPosition(progress.getLastPosition())
                .videoDuration(progress.getVideoDuration())
                .startedAt(progress.getStartedAt())
                .completedAt(progress.getCompletedAt())
                .build();
    }

    private UserRoadmapProgressResponseDTO toRoadmapDto(UserRoadmapProgress rp) {
        return UserRoadmapProgressResponseDTO.builder()
                .id(rp.getId())
                .userId(rp.getUser().getId())
                .roadmapId(rp.getAssignment().getId())
                .status(rp.getStatus().name())
                .currentItemId(rp.getCurrentItem() != null ? rp.getCurrentItem().getId() : null)
                .completedItems(rp.getCompletedItems())
                .totalItems(rp.getTotalItems())
                .startedAt(rp.getStartedAt())
                .completedAt(rp.getCompletedAt())
                .updatedAt(rp.getUpdatedAt())
                .build();
    }
}
