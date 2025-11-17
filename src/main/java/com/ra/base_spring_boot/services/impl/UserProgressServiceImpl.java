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
        return toSessionDto(progress);
    }

    @Override
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
        if (progress.getStatus() == LessonProgressStatus.IN_PROGRESS && progress.getStartedAt() == null) {
            progress.setStartedAt(LocalDateTime.now());
        }
        if (progress.getStatus() == LessonProgressStatus.COMPLETED && progress.getCompletedAt() == null) {
            progress.setCompletedAt(LocalDateTime.now());
        }

        userLessonProgressRepository.save(progress);
        return toLessonDto(progress);
    }

    @Override
    public List<UserLessonProgressResponseDTO> getLessonProgressByUserAndCourse(Long userId, Long courseId) {
        return userLessonProgressRepository.findByUserIdAndCourseId(userId, courseId)
                .stream()
                .map(this::toLessonDto)
                .toList();
    }

    // ===== Helpers =====
    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy user với id = " + userId));
    }

    private Course requireCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy khóa học với id = " + courseId));
    }

    private Session requireSession(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy session với id = " + sessionId));
    }

    private Lesson requireLesson(Long lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy lesson với id = " + lessonId));
    }

    private BigDecimal normalizePercent(BigDecimal value) {
        if (value == null) return BigDecimal.ZERO;
        if (value.compareTo(BigDecimal.ZERO) < 0) return BigDecimal.ZERO;
        if (value.compareTo(BigDecimal.valueOf(100)) > 0) return BigDecimal.valueOf(100);
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
            throw new HttpBadRequest("Loại bài học không hợp lệ (VIDEO/EXERCISE/QUIZ)");
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
                .startedAt(progress.getStartedAt())
                .completedAt(progress.getCompletedAt())
                .build();
    }
}


