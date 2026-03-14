package com.ra.base_spring_boot.services.exam.impl;

import com.ra.base_spring_boot.dto.Exam.ExamRequestDTO;
import com.ra.base_spring_boot.dto.Exam.ExamResponseDTO;
import com.ra.base_spring_boot.model.*;
import com.ra.base_spring_boot.model.constants.ExamStatus;
import com.ra.base_spring_boot.repository.exam.IExamRepository;
import com.ra.base_spring_boot.repository.exam.IQuestionRepository;
import com.ra.base_spring_boot.services.exam.IExamService;
import com.ra.base_spring_boot.dto.questions.QuestionResponseDTO;
import lombok.RequiredArgsConstructor;
import com.ra.base_spring_boot.repository.user.IUserRepository;
import com.ra.base_spring_boot.model.constants.RoleName;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ra.base_spring_boot.security.principle.MyUserDetails;
import com.ra.base_spring_boot.repository.registration.IRegistrationRepository;
import com.ra.base_spring_boot.repository.classroom.IClassStudentRepository;
import com.ra.base_spring_boot.model.constants.PaymentStatus;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.exception.HttpForbiden;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements IExamService {

        private final IExamRepository examRepository;
        private final IQuestionRepository questionRepository;
        private final IUserRepository userRepository;
        private final IRegistrationRepository registrationRepository;
        private final IClassStudentRepository classStudentRepository;
        private final com.ra.base_spring_boot.repository.course.IClassCourseRepository classCourseRepository;

        private User getCurrentUser() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.getPrincipal() instanceof MyUserDetails) {
                        return ((MyUserDetails) authentication.getPrincipal()).getUser();
                }
                throw new HttpBadRequest("Không xác định được người dùng hiện tại");
        }

        private boolean hasAccessToExam(Exam exam, User user) {
                if (user.getRole() == RoleName.ROLE_ADMIN || user.getRole() == RoleName.ROLE_TEACHER) {
                        return true;
                }

                // For student, check enrollments
                boolean hasCourseAccess = false;
                if (exam.getCourseId() != null) {
                        var registrations = registrationRepository.findByStudent_Id(user.getId());
                        hasCourseAccess = registrations.stream()
                                        .anyMatch(r -> r.getCourse().getId().equals(exam.getCourseId())
                                                        && r.getPaymentStatus() == PaymentStatus.PAID);
                }

                boolean hasClassAccess = false;
                if (exam.getClassId() != null) {
                        hasClassAccess = classStudentRepository.existsByClassroomIdAndStudentId(exam.getClassId(),
                                        user.getId());
                }

                // Allow if they have access through EITHER the course OR the class
                // (e.g., an exam might only be assigned to a course, or only to a class)
                // If it's assigned to neither (both null), perhaps it's a public exam?
                // Let's assume if both are null, it's not accessible.
                if (exam.getCourseId() == null && exam.getClassId() == null) {
                        return false;
                }

                return hasCourseAccess || hasClassAccess;
        }

        private void enforceExamTimeForStudent(Exam exam, User user) {
                if (user.getRole() != RoleName.ROLE_USER)
                        return;
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime start = exam.getStartTime();
                LocalDateTime end = exam.getEndTime();
                if (start != null && now.isBefore(start)) {
                        throw new HttpForbiden("Bài kiểm tra chưa đến thời gian làm.");
                }
                if (end != null && now.isAfter(end)) {
                        throw new HttpForbiden("Bài kiểm tra đã kết thúc.");
                }
        }

        // ======= Tạo kỳ thi (ADMIN) =======
        @Override
        @Transactional
        public ExamResponseDTO createExam(ExamRequestDTO dto) {
                User creator = null;
                if (dto.getCreatorId() != null) {
                        creator = userRepository.findById(dto.getCreatorId()).orElse(null);
                }

                Exam exam = Exam.builder()
                                .title(dto.getTitle())
                                .description(dto.getDescription())
                                .maxScore(dto.getMaxScore())
                                .passingScore(dto.getPassingScore())
                                .durationMinutes(dto.getDurationMinutes())
                                .startTime(dto.getStartTime())
                                .endTime(dto.getEndTime())
                                .courseId(dto.getCourseId())
                                .classId(dto.getClassId())
                                .creator(creator)
                                .status(ExamStatus.UPCOMING)
                                .createdAt(LocalDateTime.now())
                                .examQuestions(new ArrayList<>())
                                .totalQuestions(0)
                                .build();

                // ======= Thêm câu hỏi theo yêu cầu =======
                if (dto.isAutoAddQuestions()) {
                        List<Question> questions = questionRepository.findAll();
                        int orderIndex = 1;
                        for (Question q : questions) {
                                ExamQuestion eq = ExamQuestion.builder()
                                                .exam(exam)
                                                .question(q)
                                                .orderIndex(orderIndex++)
                                                .build();
                                exam.getExamQuestions().add(eq);
                        }
                } else if (dto.getQuestionIds() != null && !dto.getQuestionIds().isEmpty()) {
                        int orderIndex = 1;
                        for (Long qId : dto.getQuestionIds()) {
                                Question q = questionRepository.findById(qId)
                                                .orElseThrow(() -> new RuntimeException("Question not found: " + qId));
                                ExamQuestion eq = ExamQuestion.builder()
                                                .exam(exam)
                                                .question(q)
                                                .orderIndex(orderIndex++)
                                                .build();
                                exam.getExamQuestions().add(eq);
                        }
                }

                exam.setTotalQuestions(exam.getExamQuestions().size());

                examRepository.save(exam);

                return mapToResponse(exam);
        }

        // ======= Cập nhật kỳ thi (ADMIN) =======
        @Override
        @Transactional
        public ExamResponseDTO updateExam(Long examId, ExamRequestDTO dto) {
                Exam exam = examRepository.findById(examId)
                                .orElseThrow(() -> new RuntimeException("Exam not found"));

                exam.setTitle(dto.getTitle());
                exam.setDescription(dto.getDescription());
                exam.setMaxScore(dto.getMaxScore());
                exam.setPassingScore(dto.getPassingScore());
                exam.setDurationMinutes(dto.getDurationMinutes());
                exam.setStartTime(dto.getStartTime());
                exam.setEndTime(dto.getEndTime());
                exam.setCourseId(dto.getCourseId());
                exam.setClassId(dto.getClassId());
                exam.setUpdatedAt(LocalDateTime.now());

                examRepository.save(exam);
                return mapToResponse(exam);
        }

        // ======= Xóa kỳ thi (ADMIN) =======
        @Override
        @Transactional
        public void deleteExam(Long examId) {
                Exam exam = examRepository.findById(examId)
                                .orElseThrow(() -> new RuntimeException("Exam not found"));

                // Force load lazy collections trước khi xóa
                exam.getExamQuestions().size();
                if (exam.getExamAttempts() != null) {
                        exam.getExamAttempts().size();
                }

                // Xóa tất cả child
                exam.getExamQuestions().clear();
                if (exam.getExamAttempts() != null) {
                        exam.getExamAttempts().clear();
                }

                // Xóa parent
                examRepository.delete(exam);
        }

        // ======= Lấy kỳ thi theo ID =======
        @Override
        @Transactional(readOnly = true)
        public ExamResponseDTO getExam(Long examId) {
                Exam exam = examRepository.findById(examId)
                                .orElseThrow(() -> new RuntimeException("Exam not found"));

                User currentUser = getCurrentUser();
                if (!hasAccessToExam(exam, currentUser)) {
                        throw new HttpBadRequest(
                                        "Bạn không có quyền truy cập kỳ thi này vì chưa đăng ký khóa học/lớp học tương ứng.");
                }
                enforceExamTimeForStudent(exam, currentUser);

                return mapToResponse(exam);
        }

        // ======= Lấy danh sách tất cả kỳ thi =======
        @Override
        @Transactional(readOnly = true)
        public List<ExamResponseDTO> getAllExams() {
                User currentUser = getCurrentUser();

                return examRepository.findAll()
                                .stream()
                                .filter(exam -> hasAccessToExam(exam, currentUser))
                                .filter(exam -> {
                                        if (currentUser.getRole() != RoleName.ROLE_USER)
                                                return true;
                                        LocalDateTime now = LocalDateTime.now();
                                        LocalDateTime start = exam.getStartTime();
                                        LocalDateTime end = exam.getEndTime();
                                        if (start != null && now.isBefore(start))
                                                return false;
                                        if (end != null && now.isAfter(end))
                                                return false;
                                        return true;
                                })
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        // ======= Thêm câu hỏi hiện có vào kỳ thi =======
        @Override
        @Transactional
        public void addQuestionsToExam(Long examId, List<Long> questionIds) {
                Exam exam = examRepository.findById(examId)
                                .orElseThrow(() -> new RuntimeException("Exam not found"));

                for (Long questionId : questionIds) {
                        Question question = questionRepository.findById(questionId)
                                        .orElseThrow(() -> new RuntimeException("Question not found: " + questionId));

                        boolean exists = exam.getExamQuestions().stream()
                                        .anyMatch(eq -> eq.getQuestion().getId().equals(questionId));
                        if (exists)
                                continue;

                        ExamQuestion eq = ExamQuestion.builder()
                                        .exam(exam)
                                        .question(question)
                                        .orderIndex(exam.getExamQuestions().size() + 1)
                                        .build();

                        exam.getExamQuestions().add(eq);
                }

                exam.setTotalQuestions(exam.getExamQuestions().size());
                examRepository.save(exam);
        }

        // ======= Chuyển Entity -> DTO (bao gồm danh sách câu hỏi) =======
        private ExamResponseDTO mapToResponse(Exam exam) {
                double pointPerQuestion = exam.getTotalQuestions() > 0
                                ? exam.getMaxScore() * 1.0 / exam.getTotalQuestions()
                                : 0;

                List<QuestionResponseDTO> questions = exam.getExamQuestions().stream()
                                .map(eq -> QuestionResponseDTO.builder()
                                                .id(eq.getQuestion().getId())
                                                .questionText(eq.getQuestion().getQuestionText())
                                                .options(eq.getQuestion().getOptions())
                                                .correctAnswer(eq.getQuestion().getCorrectAnswer())
                                                .explanation(eq.getQuestion().getExplanation())
                                                .score(pointPerQuestion)
                                                .build())
                                .collect(Collectors.toList());

                return ExamResponseDTO.builder()
                                .id(exam.getId())
                                .title(exam.getTitle())
                                .description(exam.getDescription())
                                .totalQuestions(exam.getTotalQuestions())
                                .maxScore(exam.getMaxScore())
                                .passingScore(exam.getPassingScore())
                                .durationMinutes(exam.getDurationMinutes())
                                .startTime(exam.getStartTime())
                                .endTime(exam.getEndTime())
                                .status(exam.getStatus() != null ? exam.getStatus().name() : null)
                                .questions(questions)
                                .createdAt(exam.getCreatedAt())
                                .updatedAt(exam.getUpdatedAt())
                                .courseId(exam.getCourseId())
                                .classId(exam.getClassId())
                                .creatorName(exam.getCreator() != null ? exam.getCreator().getFullName() : "Admin")
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public List<ExamResponseDTO> getExamsByClass(Long classId) {
                User currentUser = getCurrentUser();
                if (currentUser.getRole() == RoleName.ROLE_USER) {
                        if (!classStudentRepository.existsByClassroomIdAndStudentId(classId, currentUser.getId())) {
                                throw new HttpBadRequest("Bạn không có quyền truy cập vì không thuộc lớp học này.");
                        }
                }

                // Get all courses assigned to this class to fetch related exams
                var classCourses = classCourseRepository.findByClazzId(classId);
                List<Long> courseIds = classCourses.stream()
                                .map(cc -> cc.getCourse().getId())
                                .collect(Collectors.toList());

                // Find exams directly linked to class OR linked to any course assigned to this class
                List<Exam> exams = new ArrayList<>(examRepository.findByClassId(classId));
                if (courseIds != null && !courseIds.isEmpty()) {
                        exams.addAll(examRepository.findByCourseIdIn(courseIds));
                }

                // Deduplicate by ID
                return exams.stream()
                                .filter(distinctByKey(Exam::getId))
                                .filter(exam -> {
                                        if (currentUser.getRole() != RoleName.ROLE_USER)
                                                return true;
                                        LocalDateTime now = LocalDateTime.now();
                                        LocalDateTime start = exam.getStartTime();
                                        LocalDateTime end = exam.getEndTime();
                                        if (start != null && now.isBefore(start))
                                                return false;
                                        if (end != null && now.isAfter(end))
                                                return false;
                                        return true;
                                })
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        private static <T> java.util.function.Predicate<T> distinctByKey(java.util.function.Function<? super T, ?> keyExtractor) {
                java.util.Map<Object, Boolean> seen = new java.util.concurrent.ConcurrentHashMap<>();
                return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
        }

        @Override
        @Transactional(readOnly = true)
        public List<ExamResponseDTO> getExamsByCourse(Long courseId) {
                User currentUser = getCurrentUser();
                if (currentUser.getRole() == RoleName.ROLE_USER) {
                        var registrations = registrationRepository.findByStudent_Id(currentUser.getId());
                        boolean enrolledInCourse = registrations.stream()
                                        .anyMatch(r -> r.getCourse().getId().equals(courseId)
                                                        && r.getPaymentStatus() == PaymentStatus.PAID);
                        if (!enrolledInCourse) {
                                throw new HttpBadRequest("Bạn chưa đăng ký khóa học này.");
                        }
                }

                return examRepository.findByCourseId(courseId)
                                .stream()
                                .filter(exam -> {
                                        if (currentUser.getRole() != RoleName.ROLE_USER)
                                                return true;
                                        LocalDateTime now = LocalDateTime.now();
                                        LocalDateTime start = exam.getStartTime();
                                        LocalDateTime end = exam.getEndTime();
                                        if (start != null && now.isBefore(start))
                                                return false;
                                        if (end != null && now.isAfter(end))
                                                return false;
                                        return true;
                                })
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }
}
