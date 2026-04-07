package com.ra.base_spring_boot.services.analytics.impl;

import com.ra.base_spring_boot.dto.analytics.MasteryMetricDTO;
import com.ra.base_spring_boot.dto.analytics.SkillMasteryDTO;
import com.ra.base_spring_boot.dto.Classroom.ClassroomResponseDTO;
import com.ra.base_spring_boot.dto.analytics.StudentRiskDTO;
import com.ra.base_spring_boot.dto.analytics.StudentSuccessAnalyticsResponse;
import com.ra.base_spring_boot.dto.analytics.TeacherWorkloadDTO;
import com.ra.base_spring_boot.model.attendance.AttendanceRecord;
import com.ra.base_spring_boot.model.ClassStudent;
import com.ra.base_spring_boot.model.ClassTeacher;
import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.model.constants.AttendanceStatus;
import com.ra.base_spring_boot.model.constants.LessonType;
import com.ra.base_spring_boot.model.constants.RoleName;
import com.ra.base_spring_boot.repository.classroom.IClassRepository;
import com.ra.base_spring_boot.repository.classroom.IClassStudentRepository;
import com.ra.base_spring_boot.repository.classroom.IClassTeacherRepository;
import com.ra.base_spring_boot.repository.classroom.IAttendanceRecordRepository;
import com.ra.base_spring_boot.repository.classroom.IScheduleItemRepository;
import com.ra.base_spring_boot.repository.course.IClassCourseRepository;
import com.ra.base_spring_boot.repository.user.IUserCourseProgressRepository;
import com.ra.base_spring_boot.repository.user.IUserLessonProgressRepository;
import com.ra.base_spring_boot.services.analytics.IStudentSuccessAnalyticsService;
import com.ra.base_spring_boot.services.classroom.IClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentSuccessAnalyticsServiceImpl implements IStudentSuccessAnalyticsService {

    private final IClassRepository classRepository;
    private final IClassStudentRepository classStudentRepository;
    private final IAttendanceRecordRepository attendanceRecordRepository;
    private final IClassCourseRepository classCourseRepository;
    private final IUserCourseProgressRepository userCourseProgressRepository;
    private final IUserLessonProgressRepository userLessonProgressRepository;
    private final IClassTeacherRepository classTeacherRepository;
    private final IScheduleItemRepository scheduleItemRepository;
    private final IClassService classService;

    
@Override
@Transactional(readOnly = true)
public StudentSuccessAnalyticsResponse getByClass(Long classId, User currentUser) {
    var clazz = classRepository.findById(classId)
            .orElseThrow(() -> new IllegalArgumentException("Class not found: " + classId));

    if (currentUser != null) {
        if (!isAdmin()) {
            if (!classTeacherRepository.existsByClazzIdAndTeacherId(classId, currentUser.getId())) {
                throw new AccessDeniedException("Bạn không được phân quyền xem phân tích lớp này.");
            }
        }
    }

    List<ClassStudent> classStudents = classStudentRepository.findByClassroomIdWithRelations(classId);
    Set<Long> studentIds = classStudents.stream()
            .map(cs -> cs.getStudent().getId())
            .collect(Collectors.toSet());

    List<AttendanceRecord> attendanceRecords = studentIds.isEmpty()
            ? Collections.emptyList()
            : attendanceRecordRepository.findByClassroomId(classId);

    Map<Long, StudentMetric> metricMap = new HashMap<>();
    for (Long studentId : studentIds) {
        metricMap.put(studentId, new StudentMetric(studentId));
    }

    for (AttendanceRecord record : attendanceRecords) {
        if (record.getStudent() == null) continue;
        StudentMetric metric = metricMap.get(record.getStudent().getId());
        if (metric == null) continue;
        metric.totalRecords++;
        if (record.getStatus() == AttendanceStatus.PRESENT
                || record.getStatus() == AttendanceStatus.LATE
                || record.getStatus() == AttendanceStatus.EXCUSED) {
            metric.presentRecords++;
        }
    }

    Long courseId = classCourseRepository.findByClazzId(classId)
            .stream()
            .map(cc -> cc.getCourse().getId())
            .findFirst()
            .orElse(null);

    Map<Long, Double> progressByStudent = new HashMap<>();
    if (courseId != null) {
        userCourseProgressRepository.findByCourse_Id(courseId)
                .forEach(progress -> {
                    if (studentIds.contains(progress.getUser().getId())) {
                        progressByStudent.put(progress.getUser().getId(),
                                toDouble(progress.getProgressPercent()));
                    }
                });
    }

    Map<Long, Double> scoreByStudent = classStudents.stream()
            .collect(Collectors.toMap(
                    cs -> cs.getStudent().getId(),
                    cs -> toDouble(cs.getFinalScore()),
                    (first, second) -> Math.max(first, second)));

    List<StudentRiskDTO> riskList = new ArrayList<>();
    List<Double> attendanceRatios = new ArrayList<>();
    List<Double> attendancePercents = new ArrayList<>();
    List<Double> progressValues = new ArrayList<>();
    List<Double> scoreValues = new ArrayList<>();

    for (var entry : metricMap.values()) {
        double attendanceRate = entry.getAttendanceRate();
        double attendancePercent = attendanceRate * 100d;
        double progressPercent = progressByStudent.getOrDefault(entry.studentId, 0d);
        double scorePercent = scoreByStudent.getOrDefault(entry.studentId, 0d);

        attendanceRatios.add(attendanceRate);
        attendancePercents.add(attendancePercent);
        progressValues.add(progressPercent);
        scoreValues.add(scorePercent);

        StringBuilder reasons = new StringBuilder();
        if (attendancePercent < 70) {
            reasons.append("Tỷ lệ điểm danh thấp");
        }
        if (progressPercent < 60) {
            if (reasons.length() > 0) reasons.append("; ");
            reasons.append("Tiến độ khóa học chưa đủ");
        }
        if (scorePercent > 0 && scorePercent < 60) {
            if (reasons.length() > 0) reasons.append("; ");
            reasons.append("Điểm số tổng kết thấp");
        }

        if (reasons.length() > 0) {
            var student = classStudents.stream()
                    .filter(cs -> cs.getStudent().getId().equals(entry.studentId))
                    .findFirst()
                    .map(cs -> cs.getStudent().getFullName())
                    .orElse("Học viên");
            riskList.add(StudentRiskDTO.builder()
                    .studentId(entry.studentId)
                    .studentName(student)
                    .attendanceRate(attendancePercent)
                    .progressPercent(progressPercent)
                    .scorePercent(scorePercent)
                    .riskFactors(reasons.toString())
                    .build());
        }
    }

    riskList.sort((a, b) -> Double.compare(getRiskScore(b), getRiskScore(a)));
    if (riskList.size() > 10) {
        riskList = riskList.subList(0, 10);
    }

    double averageAttendance = attendanceRatios.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0d);
    double averageProgress = progressValues.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0d);
    double averageScore = scoreValues.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0d);

    double attendanceProgressCorrelation = calculatePearson(attendancePercents, progressValues);
    double attendanceScoreCorrelation = calculatePearson(attendancePercents, scoreValues);
    double progressScoreCorrelation = calculatePearson(progressValues, scoreValues);

    List<MasteryMetricDTO> masteryMetrics = buildMasteryMetrics(courseId, studentIds);
    List<SkillMasteryDTO> skillMastery = buildSkillMasteryMetrics(courseId, studentIds);
    List<TeacherWorkloadDTO> teacherWorkloads = buildTeacherWorkloads(classId);

    return StudentSuccessAnalyticsResponse.builder()
            .classId(classId)
            .className(clazz.getClassName())
            .averageAttendanceRate(averageAttendance)
            .averageProgressPercent(averageProgress)
            .averageScorePercent(averageScore)
            .attendanceProgressCorrelation(attendanceProgressCorrelation)
            .attendanceScoreCorrelation(attendanceScoreCorrelation)
            .progressScoreCorrelation(progressScoreCorrelation)
            .atRiskStudents(riskList)
            .masteryByLessonType(masteryMetrics)
            .masteryBySkill(skillMastery)
            .teacherWorkloads(teacherWorkloads)
            .build();
}

private List<MasteryMetricDTO> buildMasteryMetrics(Long courseId, Set<Long> studentIds) {
        if (courseId == null || studentIds.isEmpty()) {
            return List.of();
        }
        List<MasteryMetricDTO> result = new ArrayList<>();
        for (LessonType type : LessonType.values()) {
            List<Double> values = userLessonProgressRepository
                    .findByCourse_IdAndLesson_Type(courseId, type)
                    .stream()
                    .filter(progress -> studentIds.contains(progress.getUser().getId()))
                    .map(progress -> toDouble(progress.getProgressPercent()))
                    .collect(Collectors.toList());
            double avg = values.stream().mapToDouble(Double::doubleValue).average().orElse(0d);
            result.add(MasteryMetricDTO.builder()
                    .lessonType(type.name())
                    .averageProgressPercent(avg)
                    .build());
        }
        return result;
    }

    
    private List<SkillMasteryDTO> buildSkillMasteryMetrics(Long courseId, Set<Long> studentIds) {
        if (courseId == null || studentIds.isEmpty()) {
            return List.of();
        }
        Map<String, SkillAggregate> aggregates = new HashMap<>();
        userLessonProgressRepository.findByCourse_Id(courseId)
                .stream()
                .filter(progress -> studentIds.contains(progress.getUser().getId()))
                .forEach(progress -> {
                    var lesson = progress.getLesson();
                    if (lesson == null) {
                        return;
                    }
                    String skillName = lesson.getTitle();
                    if (skillName == null || skillName.isBlank()) {
                        return;
                    }
                    skillName = skillName.trim();
                    var aggregate = aggregates.computeIfAbsent(skillName, key -> new SkillAggregate());
                    aggregate.totalProgress += toDouble(progress.getProgressPercent());
                    aggregate.entries++;
                    aggregate.studentIds.add(progress.getUser().getId());
                });
        return aggregates.entrySet().stream()
                .map(entry -> SkillMasteryDTO.builder()
                        .skillName(entry.getKey())
                        .averageProgressPercent(entry.getValue().average())
                        .studentCount(entry.getValue().studentIds.size())
                        .build())
                .sorted((a, b) -> Double.compare(b.getAverageProgressPercent(), a.getAverageProgressPercent()))
                .toList();
    }



private List<TeacherWorkloadDTO> buildTeacherWorkloads(Long classId) {
        List<ClassTeacher> teachers = classTeacherRepository.findByClazzId(classId);
        if (teachers.isEmpty()) return List.of();

        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);
        Set<Long> classIds = teachers.stream().map(ct -> ct.getClazz().getId()).collect(Collectors.toSet());

        List<Long> classIdList = new ArrayList<>(classIds);
        Map<Long, Long> upcomingSessionsByClass = new HashMap<>();
        if (!classIds.isEmpty()) {
            scheduleItemRepository.findByClassIdsAndDateBetween(classIdList, today, nextWeek)
                    .forEach(si -> {
                        Long cid = si.getClassCourse().getClazz().getId();
                        upcomingSessionsByClass.merge(cid, 1L, (existing, newValue) -> existing + newValue);
                    });
        }

        return teachers.stream()
                .map(teacher -> {
                    Long teacherId = teacher.getTeacher().getId();
                    long teacherClassCount = classTeacherRepository.countByTeacherId(teacherId);
                    List<ClassTeacher> teacherAssignments = classTeacherRepository.findByTeacherId(teacherId);
                    long upcoming = teacherAssignments.stream()
                            .map(ct -> upcomingSessionsByClass.getOrDefault(ct.getClazz().getId(), 0L))
                            .reduce(0L, (existing, newValue) -> existing + newValue);
                    long activeStudents = teacherAssignments.stream()
                            .mapToLong(ct -> classStudentRepository.countByClassroomId(ct.getClazz().getId()))
                            .sum();

                    return TeacherWorkloadDTO.builder()
                            .teacherId(teacherId)
                            .teacherName(teacher.getTeacher().getFullName())
                            .classCount(teacherClassCount)
                            .upcomingSessionsNext7Days(upcoming)
                            .activeStudents(activeStudents)
                            .build();
                })
                .collect(Collectors.toList());
    }

@Override
    @Transactional(readOnly = true)
    public List<ClassroomResponseDTO> getAccessibleClasses(User currentUser) {
        if (currentUser == null) {
            return Collections.emptyList();
        }
        if (isAdmin()) {
            return classService.findAll();
        }
        if (currentUser.getRole() == RoleName.ROLE_TEACHER) {
            return classService.findClassesByTeacher(currentUser.getId());
        }
        return Collections.emptyList();
    }

    private double getRiskScore(StudentRiskDTO dto) {
        double attendancePenalty = Math.max(70 - dto.getAttendanceRate(), 0d) / 100d;
        double progressPenalty = Math.max(60 - dto.getProgressPercent(), 0d) / 100d;
        double scorePenalty = dto.getScorePercent() > 0
                ? Math.max(60 - dto.getScorePercent(), 0d) / 100d
                : 0d;
        return attendancePenalty + progressPenalty + scorePenalty;
    }

    private double calculatePearson(List<Double> x, List<Double> y) {
        if (x.size() < 2 || y.size() < 2 || x.size() != y.size()) {
            return 0d;
        }
        double meanX = x.stream().mapToDouble(Double::doubleValue).average().orElse(0d);
        double meanY = y.stream().mapToDouble(Double::doubleValue).average().orElse(0d);

        double numerator = 0d;
        double sumX = 0d;
        double sumY = 0d;

        for (int i = 0; i < x.size(); i++) {
            double dx = x.get(i) - meanX;
            double dy = y.get(i) - meanY;
            numerator += dx * dy;
            sumX += dx * dx;
            sumY += dy * dy;
        }

        if (sumX == 0d || sumY == 0d) {
            return 0d;
        }

        return numerator / Math.sqrt(sumX * sumY);
    }

    private double toDouble(BigDecimal value) {
        return value != null ? value.doubleValue() : 0d;
    }

    private static class StudentMetric {
        final Long studentId;
        long presentRecords;
        long totalRecords;

        StudentMetric(Long studentId) {
            this.studentId = studentId;
        }

        double getAttendanceRate() {
            return totalRecords == 0 ? 0d : (double) presentRecords / totalRecords;
        }
    }
private static class SkillAggregate {
    double totalProgress;
    int entries;
    Set<Long> studentIds = new HashSet<>();

    double average() {
        return entries == 0 ? 0d : totalProgress / entries;
    }
}


    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities() != null && auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

}
