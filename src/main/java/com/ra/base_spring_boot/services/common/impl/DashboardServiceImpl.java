package com.ra.base_spring_boot.services.common.impl;

import com.ra.base_spring_boot.dto.DashBoardStats.DashboardStatsDTO;
import com.ra.base_spring_boot.dto.DashBoardStats.CourseProgressDTO;
import com.ra.base_spring_boot.dto.DashBoardStats.QuizReportDTO;
import com.ra.base_spring_boot.dto.DashBoardStats.UserGrowthPointDTO;
import com.ra.base_spring_boot.dto.Exam.RecentExamDTO;
import com.ra.base_spring_boot.dto.resp.UserResponse;
import com.ra.base_spring_boot.dto.Course.CourseResponseDTO;
import com.ra.base_spring_boot.dto.LessonQuizzes.LessonQuizResponseDTO;
import com.ra.base_spring_boot.model.*;
import com.ra.base_spring_boot.model.constants.RoleName;
import com.ra.base_spring_boot.repository.common.IAssignmentRepository;
import com.ra.base_spring_boot.repository.classroom.IClassRepository;
import com.ra.base_spring_boot.repository.course.ICourseRepository;
import com.ra.base_spring_boot.repository.course.ILessonQuizRepository;
import com.ra.base_spring_boot.repository.exam.IExamAttemptRepository;
import com.ra.base_spring_boot.repository.exam.IExamRepository;
import com.ra.base_spring_boot.repository.quiz.IQuizResultRepository;
import com.ra.base_spring_boot.repository.user.IUserCourseRepository;
import com.ra.base_spring_boot.repository.user.IUserRepository;
import com.ra.base_spring_boot.services.common.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements IDashboardService {

        private final IUserRepository userRepo;
        private final ICourseRepository courseRepo;
        private final ILessonQuizRepository quizRepo;
        private final IQuizResultRepository quizResultRepo;
        private final IUserCourseRepository userCourseRepo;
        private final IAssignmentRepository assignmentRepo;
        private final IClassRepository classRepo;
        private final IExamAttemptRepository examAttemptRepository;
        private final IExamRepository examRepository;

        private double calcGrowth(long current, long previous) {
                if (previous == 0)
                        return current > 0 ? 100.0 : 0.0;
                return ((double) (current - previous) / previous) * 100.0;
        }

        private double calcGrowthDouble(Double current, Double previous) {
                if (previous == null || previous == 0)
                        return (current != null && current > 0) ? 100.0 : 0.0;
                return ((current - previous) / previous) * 100.0;
        }

        public DashboardStatsDTO getDashboard() {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime sinceMonth = now.minusMonths(1);
                LocalDateTime since30 = now.minusDays(30);

                // --- Users ---
                long totalUsers = userRepo.countByRole(RoleName.ROLE_USER);
                long prevMonthUsers = userRepo.countByRoleBefore(RoleName.ROLE_USER, sinceMonth);
                double userGrowth = calcGrowth(totalUsers, prevMonthUsers);

                // --- Courses ---
                long totalCourses = courseRepo.count();
                long prevMonthCourses = courseRepo.countBefore(sinceMonth);
                double courseGrowth = calcGrowth(totalCourses, prevMonthCourses);

                // --- Quizzes / Exams metadata ---
                long totalQuizzes = quizRepo.count();
                long prevMonthQuizzes = quizRepo.countByCreatedAtAfter(sinceMonth);
                double quizGrowth = calcGrowth(totalQuizzes, prevMonthQuizzes);

                // --- Assignments (optional) ---
                long totalAssignments = assignmentRepo != null ? assignmentRepo.count() : 0;
                long prevMonthAssignments = assignmentRepo != null ? assignmentRepo.countByCreatedAtAfter(sinceMonth)
                                : 0;
                double assignmentGrowth = calcGrowth(totalAssignments, prevMonthAssignments);

                // --- Classes (optional) ---
                long totalClasses = classRepo != null ? classRepo.count() : 0;
                long prevMonthClasses = classRepo != null ? classRepo.countByCreatedAtAfter(sinceMonth) : 0;
                double classGrowth = calcGrowth(totalClasses, prevMonthClasses);

                // --- Exam attempts and avg score (QuizResult / ExamAttempt) ---
                long totalExamAttempts = quizResultRepo.countAllAttempts();
                long prevMonthExamAttempts = quizResultRepo.countAttemptsSince(sinceMonth);
                double examAttemptGrowth = calcGrowth(totalExamAttempts, prevMonthExamAttempts);

                Double avgScore = Optional.ofNullable(
                                examAttemptRepository.avgExamScoreOnTen()).orElse(0.0);

                Double prevAvgScore = Optional.ofNullable(
                                examAttemptRepository.avgExamScoreOnTenSince(sinceMonth)).orElse(0.0);

                double avgScoreGrowth = calcGrowthDouble(avgScore, prevAvgScore);

                // --- Course completion (UserCourse / UserEnrollment) ---
                long totalEnrollments = userCourseRepo.countTotal();
                long totalCompleted = userCourseRepo.countCompleted();
                double completionRate = totalEnrollments == 0 ? 0.0
                                : ((double) totalCompleted / totalEnrollments) * 100.0;

                long prevTotalEnrollments = userCourseRepo.countTotalBefore(sinceMonth);
                long prevCompleted = userCourseRepo.countCompletedBefore(sinceMonth);
                double prevCompletionRate = prevTotalEnrollments == 0 ? 0.0
                                : ((double) prevCompleted / prevTotalEnrollments) * 100.0;
                double completionGrowth = calcGrowthDouble(completionRate, prevCompletionRate);

                // --- New users in last 30 days ---
                List<UserResponse> newUsers = userRepo.findNewUsersSince(RoleName.ROLE_USER, since30)
                                .stream().map(u -> UserResponse.builder()
                                                .id(u.getId())
                                                .fullName(u.getFullName())
                                                .gmail(u.getGmail())
                                                .role(u.getRole())
                                                .isActive(u.getIsActive())
                                                .createdAt(u.getCreatedAt())
                                                .build())
                                .collect(Collectors.toList());

                // --- New courses in last 30 days ---
                List<CourseResponseDTO> newCourses = courseRepo.findNewCoursesSince(since30)
                                .stream()
                                .map(c -> CourseResponseDTO.builder()
                                                .id(c.getId())
                                                .title(c.getTitle())
                                                .description(c.getDescription())
                                                .level(c.getLevel().name()) // convert enum -> String
                                                .createdAt(c.getCreatedAt())
                                                .build())
                                .toList();

                // --- Recent quizzes in last 30 days ---
                List<LessonQuizResponseDTO> recentQuizzes = quizRepo.findRecentSince(since30).stream()
                                .map(q -> LessonQuizResponseDTO.builder()
                                                .quizId(q.getId())
                                                .lessonId(q.getLesson().getId())
                                                .lessonTitle(q.getLesson().getTitle())
                                                .title(q.getTitle())
                                                .questionCount(q.getQuestionCount())
                                                .maxScore(q.getMaxScore())
                                                .passingScore(q.getPassingScore())
                                                .build())
                                .collect(Collectors.toList());

                // Build DTO (assumes DashboardStatsDTO exists in your dto package)
                DashboardStatsDTO dto = DashboardStatsDTO.builder()
                                .totalUsers(new DashboardStatsDTO.GrowthItem(totalUsers, userGrowth))
                                .totalCourses(new DashboardStatsDTO.GrowthItem(totalCourses, courseGrowth))
                                .totalExams(new DashboardStatsDTO.GrowthItem(totalExamAttempts, examAttemptGrowth))
                                .averageExamScore(
                                                new DashboardStatsDTO.GrowthItem(
                                                                (long) (Math.round(avgScore * 10) / 10.0), // ví dụ 7.8
                                                                avgScoreGrowth))

                                .courseCompletionRate(
                                                new DashboardStatsDTO.GrowthItem(
                                                                (long) (Math.round(completionRate * 10) / 10.0), // ví
                                                                                                                 // dụ
                                                                                                                 // 63.4%
                                                                completionGrowth))

                                .totalClasses(new DashboardStatsDTO.GrowthItem(totalClasses, classGrowth))
                                .totalQuizzes(new DashboardStatsDTO.GrowthItem(totalQuizzes, quizGrowth))
                                .totalAssignments(new DashboardStatsDTO.GrowthItem(totalAssignments, assignmentGrowth))
                                .newUsers(newUsers)
                                .newCourses(newCourses)
                                .recentQuizzes(recentQuizzes)
                                .build();

                return dto;
        }

        @Override
        public List<UserGrowthPointDTO> getUserGrowthByMonth(int months) {
                LocalDate now = LocalDate.now();
                List<UserGrowthPointDTO> points = new ArrayList<>();

                long cumulative = 0; // ⭐ TÍCH LŨY

                for (int i = months - 1; i >= 0; i--) {
                        LocalDate start = now.minusMonths(i).withDayOfMonth(1);
                        LocalDateTime startDt = start.atStartOfDay();
                        LocalDateTime endDt = start
                                        .withDayOfMonth(start.lengthOfMonth())
                                        .atTime(23, 59, 59);

                        // user mới trong tháng
                        long newUsers = userRepo.countByRoleBetween(
                                        RoleName.ROLE_USER, startDt, endDt);

                        cumulative += newUsers;

                        points.add(UserGrowthPointDTO.builder()
                                        .period(start.toString().substring(0, 7)) // yyyy-MM
                                        .count(cumulative) // ⭐ DÙNG cumulative
                                        .build());
                }

                return points;
        }

        @Override
        public List<UserGrowthPointDTO> getUserGrowthByWeek(int weeks) {
                LocalDate today = LocalDate.now();
                List<UserGrowthPointDTO> points = new ArrayList<>();

                long cumulative = 0; // ⭐ TÍCH LŨY

                for (int i = weeks - 1; i >= 0; i--) {
                        LocalDate start = today.minusWeeks(i).with(java.time.DayOfWeek.MONDAY);
                        LocalDateTime startDt = start.atStartOfDay();
                        LocalDateTime endDt = start.plusDays(6).atTime(23, 59, 59);

                        long newUsers = userRepo.countByRoleBetween(
                                        RoleName.ROLE_USER, startDt, endDt);

                        cumulative += newUsers;

                        points.add(UserGrowthPointDTO.builder()
                                        .period(start + " ~ " + start.plusDays(6))
                                        .count(cumulative) // ⭐ cumulative
                                        .build());
                }

                return points;
        }

        @Override
        public CourseProgressDTO getCourseProgress(Long courseId) {
                long total = userCourseRepo.countByCourse_Id(courseId);
                long completed = userCourseRepo.countByCourse_IdAndCompleted(courseId, true);
                long inProgress = total - completed;
                double rate = total == 0 ? 0.0 : ((double) completed / total) * 100.0;
                return CourseProgressDTO.builder()
                                .courseId(courseId)
                                .completed(completed)
                                .inProgress(inProgress)
                                .completionRate(rate)
                                .build();
        }

        @Override
        public List<UserResponse> getNewUsersLast30Days() {
                return userRepo.findNewUsersSince(RoleName.ROLE_USER, LocalDateTime.now().minusDays(30)).stream()
                                .map(u -> UserResponse.builder()
                                                .id(u.getId()).fullName(u.getFullName())
                                                .gmail(u.getGmail()).role(u.getRole())
                                                .isActive(u.getIsActive()).createdAt(u.getCreatedAt()).build())
                                .collect(Collectors.toList());
        }

        @Override
        public List<CourseResponseDTO> getNewCoursesLast30Days() {
                return courseRepo.findNewCoursesSince(LocalDateTime.now().minusDays(30)).stream()
                                .map(c -> CourseResponseDTO.builder()
                                                .id(c.getId())
                                                .title(c.getTitle())
                                                .description(c.getDescription())
                                                .level(c.getLevel().name()) // chuyển enum sang String
                                                .createdAt(c.getCreatedAt())
                                                .build())
                                .toList();
        }

        @Override
        public List<LessonQuizResponseDTO> getRecentQuizzesLast30Days() {
                return quizRepo.findRecentSince(LocalDateTime.now().minusDays(30)).stream()
                                .map(q -> LessonQuizResponseDTO.builder()
                                                .quizId(q.getId())
                                                .lessonId(q.getLesson().getId())
                                                .lessonTitle(q.getLesson().getTitle())
                                                .title(q.getTitle())
                                                .questionCount(q.getQuestionCount())
                                                .maxScore(q.getMaxScore())
                                                .passingScore(q.getPassingScore())
                                                .build())
                                .collect(Collectors.toList());
        }

        @Override
        public List<QuizReportDTO> getQuizReports() {
                List<LessonQuiz> quizzes = quizRepo.findAll(); // LessonQuiz, không phải QuizQuestion
                return quizzes.stream().map(q -> {
                        long attempts = Optional.ofNullable(quizResultRepo.countAttemptByQuiz(q.getId())).orElse(0L);
                        long pass = Optional.ofNullable(quizResultRepo.countPassByQuiz(q.getId())).orElse(0L);
                        Double avg = Optional.ofNullable(quizResultRepo.avgScoreByQuiz(q.getId())).orElse(0.0);

                        double passRate = attempts == 0 ? 0.0 : (double) pass / attempts * 100.0;
                        return QuizReportDTO.builder()
                                        .quizId(q.getId())
                                        .title(q.getTitle())
                                        .attempts(attempts)
                                        .avgScore(avg)
                                        .passRate(passRate)
                                        .build();
                }).collect(Collectors.toList());
        }

        @Override
        public long getCompletedExams() {
                return examAttemptRepository.countCompletedExams();

        }

        @Override
        public List<RecentExamDTO> getRecentExams() {

                LocalDateTime since = LocalDateTime.now().minusDays(30);

                List<Exam> exams = examRepository.findRecentExams(since);

                return exams.stream().map(exam -> {

                        long attempts = Optional
                                        .ofNullable(examAttemptRepository.countAttemptByExam(exam.getId()))
                                        .orElse(0L);

                        long passed = Optional
                                        .ofNullable(examAttemptRepository.countPassByExam(exam.getId()))
                                        .orElse(0L);

                        double passRate = attempts == 0
                                        ? 0.0
                                        : (double) passed / attempts * 100.0;

                        return RecentExamDTO.builder()
                                        .id(exam.getId())
                                        .title(exam.getTitle())
                                        .maxScore(exam.getMaxScore())
                                        .passingScore(exam.getPassingScore())
                                        .attempts(attempts)
                                        .passRate(passRate)
                                        .createdAt(exam.getCreatedAt())
                                        .build();
                }).toList();
        }

}
