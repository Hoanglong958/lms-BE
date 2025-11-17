package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.Classroom.*;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.*;
import com.ra.base_spring_boot.model.constants.*;
import com.ra.base_spring_boot.repository.*;
import com.ra.base_spring_boot.services.IClassroomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassroomServiceImpl implements IClassroomService {

    private final IClassroomRepository classroomRepository;
    private final IClassStudentRepository classStudentRepository;
    private final IClassTeacherRepository classTeacherRepository;
    private final IClassCourseRepository classCourseRepository;
    private final IUserRepository userRepository;
    private final ICourseRepository courseRepository;

    @Override
    public ClassroomResponseDTO create(ClassroomRequestDTO dto) {
        Classroom classroom = Classroom.builder()
                .className(requireText(dto.getClassName(), "Tên lớp học không được để trống"))
                .description(dto.getDescription())
                .maxStudents(dto.getMaxStudents() == null ? 30 : dto.getMaxStudents())
                .startDate(parseDate(dto.getStartDate(), "Ngày bắt đầu lớp học không hợp lệ"))
                .endDate(parseOptionalDate(dto.getEndDate()))
                .scheduleInfo(dto.getScheduleInfo())
                .status(parseStatus(dto.getStatus()))
                .build();
        validateDateRange(classroom.getStartDate(), classroom.getEndDate());
        classroomRepository.save(classroom);
        return toClassroomDto(classroom);
    }

    @Override
    public ClassroomResponseDTO update(Long id, ClassroomRequestDTO dto) {
        Classroom classroom = getClassroom(id);
        if (dto.getClassName() != null) {
            classroom.setClassName(dto.getClassName());
        }
        if (dto.getDescription() != null) {
            classroom.setDescription(dto.getDescription());
        }
        if (dto.getMaxStudents() != null && dto.getMaxStudents() > 0) {
            classroom.setMaxStudents(dto.getMaxStudents());
        }
        if (dto.getStartDate() != null) {
            classroom.setStartDate(parseDate(dto.getStartDate(), "Ngày bắt đầu lớp học không hợp lệ"));
        }
        if (dto.getEndDate() != null) {
            classroom.setEndDate(parseOptionalDate(dto.getEndDate()));
        }
        if (dto.getScheduleInfo() != null) {
            classroom.setScheduleInfo(dto.getScheduleInfo());
        }
        if (dto.getStatus() != null) {
            classroom.setStatus(parseStatus(dto.getStatus()));
        }
        validateDateRange(classroom.getStartDate(), classroom.getEndDate());
        classroomRepository.save(classroom);
        return toClassroomDto(classroom);
    }

    @Override
    public void delete(Long id) {
        Classroom classroom = getClassroom(id);
        classroomRepository.delete(classroom);
    }

    @Override
    public ClassroomResponseDTO findById(Long id) {
        return toClassroomDto(getClassroom(id));
    }

    @Override
    public List<ClassroomResponseDTO> findAll() {
        return classroomRepository.findAll().stream()
                .map(this::toClassroomDto)
                .toList();
    }

    @Override
    public Page<ClassroomResponseDTO> findAll(Pageable pageable) {
        return classroomRepository.findAll(pageable).map(this::toClassroomDto);
    }

    @Override
    public Page<ClassroomResponseDTO> search(String keyword, Pageable pageable) {
        String kw = keyword == null ? "" : keyword.trim();
        return classroomRepository.findByClassNameContainingIgnoreCase(kw, pageable)
                .map(this::toClassroomDto);
    }

    @Override
    @Transactional
    public ClassStudentResponseDTO enrollStudent(ClassStudentRequestDTO dto) {
        Classroom classroom = getClassroom(dto.getClassId());
        User student = userRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy học viên với id = " + dto.getStudentId()));
        if (student.getRole() != RoleName.ROLE_USER) {
            throw new HttpBadRequest("student_id chỉ chấp nhận tài khoản role STUDENT (ROLE_USER)");
        }
        if (classStudentRepository.existsByClazzIdAndStudentId(classroom.getId(), student.getId())) {
            throw new HttpBadRequest("Học viên đã tồn tại trong lớp");
        }
        if (classStudentRepository.countByClazzId(classroom.getId()) >= classroom.getMaxStudents()) {
            throw new HttpBadRequest("Lớp đã đủ sĩ số tối đa");
        }
        ClassStudent enrollment = ClassStudent.builder()
                .clazz(classroom)
                .student(student)
                .status(parseEnrollmentStatus(dto.getStatus()))
                .finalScore(normalizeScore(dto.getFinalScore()))
                .attendanceRate(normalizeScore(dto.getAttendanceRate()))
                .note(dto.getNote())
                .build();
        classStudentRepository.save(enrollment);
        return toStudentDto(enrollment);
    }

    @Override
    public void removeStudent(Long classId, Long studentId) {
        ClassStudent enrollment = classStudentRepository.findByClazzIdAndStudentId(classId, studentId)
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy học viên trong lớp"));
        classStudentRepository.delete(enrollment);
    }

    @Override
    public List<ClassStudentResponseDTO> findStudents(Long classId) {
        return classStudentRepository.findByClazzId(classId)
                .stream()
                .map(this::toStudentDto)
                .toList();
    }

    @Override
    @Transactional
    public ClassTeacherResponseDTO assignTeacher(ClassTeacherRequestDTO dto) {
        Classroom classroom = getClassroom(dto.getClassId());
        User teacher = userRepository.findById(dto.getTeacherId())
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy giảng viên với id = " + dto.getTeacherId()));
        if (teacher.getRole() != RoleName.ROLE_ADMIN) {
            throw new HttpBadRequest("teacher_id chỉ chấp nhận tài khoản role INSTRUCTOR (ROLE_ADMIN)");
        }
        if (classTeacherRepository.existsByClazzIdAndTeacherId(classroom.getId(), teacher.getId())) {
            throw new HttpBadRequest("Giảng viên đã được phân công cho lớp này");
        }
        ClassTeacher assignment = ClassTeacher.builder()
                .clazz(classroom)
                .teacher(teacher)
                .role(parseTeacherRole(dto.getRole()))
                .note(dto.getNote())
                .build();
        classTeacherRepository.save(assignment);
        return toTeacherDto(assignment);
    }

    @Override
    public void removeTeacher(Long classId, Long teacherId) {
        ClassTeacher teacher = classTeacherRepository.findByClazzIdAndTeacherId(classId, teacherId)
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy giảng viên trong lớp"));
        classTeacherRepository.delete(teacher);
    }

    @Override
    public List<ClassTeacherResponseDTO> findTeachers(Long classId) {
        return classTeacherRepository.findByClazzId(classId)
                .stream()
                .map(this::toTeacherDto)
                .toList();
    }

    @Override
    @Transactional
    public ClassCourseResponseDTO assignCourse(ClassCourseRequestDTO dto) {
        Classroom classroom = getClassroom(dto.getClassId());
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy khóa học với id = " + dto.getCourseId()));
        if (classCourseRepository.existsByClazzIdAndCourseId(classroom.getId(), course.getId())) {
            throw new HttpBadRequest("Khóa học đã được gán cho lớp");
        }
        ClassCourse classCourse = ClassCourse.builder()
                .clazz(classroom)
                .course(course)
                .note(dto.getNote())
                .build();
        classCourseRepository.save(classCourse);
        return toCourseDto(classCourse);
    }

    @Override
    public void removeCourse(Long classId, Long courseId) {
        ClassCourse classCourse = classCourseRepository.findByClazzIdAndCourseId(classId, courseId)
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy khóa học trong lớp"));
        classCourseRepository.delete(classCourse);
    }

    @Override
    public List<ClassCourseResponseDTO> findCourses(Long classId) {
        return classCourseRepository.findByClazzId(classId)
                .stream()
                .map(this::toCourseDto)
                .toList();
    }

    @Override
    public ClassStatsResponseDTO getClassStats(Long classId) {
        Classroom classroom = getClassroom(classId);

        long totalStudents = classStudentRepository.countByClazzId(classId);
        long activeStudents = classStudentRepository.countByClazzIdAndStatus(classId, ClassEnrollmentStatus.ACTIVE);
        long completedStudents = classStudentRepository.countByClazzIdAndStatus(classId, ClassEnrollmentStatus.COMPLETED);
        long droppedStudents = classStudentRepository.countByClazzIdAndStatus(classId, ClassEnrollmentStatus.DROPPED);

        long totalTeachers = classTeacherRepository.countByClazzId(classId);
        long instructors = classTeacherRepository.countByClazzIdAndRole(classId, ClassTeacherRole.INSTRUCTOR);
        long assistants = classTeacherRepository.countByClazzIdAndRole(classId, ClassTeacherRole.ASSISTANT);

        long totalCourses = classCourseRepository.countByClazzId(classId);

        BigDecimal avgScore = classStudentRepository.averageFinalScoreByClassId(classId);
        BigDecimal avgAttendance = classStudentRepository.averageAttendanceRateByClassId(classId);

        return ClassStatsResponseDTO.builder()
                .classId(classroom.getId())
                .className(classroom.getClassName())
                .maxStudents(classroom.getMaxStudents())
                .totalStudents(totalStudents)
                .activeStudents(activeStudents)
                .completedStudents(completedStudents)
                .droppedStudents(droppedStudents)
                .averageFinalScore(avgScore)
                .averageAttendanceRate(avgAttendance)
                .totalTeachers(totalTeachers)
                .instructors(instructors)
                .assistants(assistants)
                .totalCourses(totalCourses)
                .build();
    }

    private Classroom getClassroom(Long id) {
        return classroomRepository.findById(id)
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy lớp học với id = " + id));
    }

    private ClassroomResponseDTO toClassroomDto(Classroom classroom) {
        long classId = classroom.getId();
        long studentCount = classStudentRepository.countByClazzId(classId);
        long teacherCount = classTeacherRepository.countByClazzId(classId);
        long courseCount = classCourseRepository.countByClazzId(classId);
        return ClassroomResponseDTO.builder()
                .id(classroom.getId())
                .className(classroom.getClassName())
                .description(classroom.getDescription())
                .maxStudents(classroom.getMaxStudents())
                .startDate(classroom.getStartDate())
                .endDate(classroom.getEndDate())
                .scheduleInfo(classroom.getScheduleInfo())
                .status(classroom.getStatus().name())
                .createdAt(classroom.getCreatedAt())
                .updatedAt(classroom.getUpdatedAt())
                .totalStudents(studentCount)
                .totalTeachers(teacherCount)
                .totalCourses(courseCount)
                .build();
    }

    private ClassStudentResponseDTO toStudentDto(ClassStudent classStudent) {
        return ClassStudentResponseDTO.builder()
                .id(classStudent.getId())
                .classId(classStudent.getClazz().getId())
                .className(classStudent.getClazz().getClassName())
                .studentId(classStudent.getStudent().getId())
                .studentName(classStudent.getStudent().getFullName())
                .status(classStudent.getStatus().name())
                .finalScore(classStudent.getFinalScore())
                .attendanceRate(classStudent.getAttendanceRate())
                .enrolledAt(classStudent.getEnrolledAt())
                .note(classStudent.getNote())
                .build();
    }

    private ClassTeacherResponseDTO toTeacherDto(ClassTeacher classTeacher) {
        return ClassTeacherResponseDTO.builder()
                .id(classTeacher.getId())
                .classId(classTeacher.getClazz().getId())
                .className(classTeacher.getClazz().getClassName())
                .teacherId(classTeacher.getTeacher().getId())
                .teacherName(classTeacher.getTeacher().getFullName())
                .role(classTeacher.getRole().name())
                .assignedAt(classTeacher.getAssignedAt())
                .note(classTeacher.getNote())
                .build();
    }

    private ClassCourseResponseDTO toCourseDto(ClassCourse classCourse) {
        return ClassCourseResponseDTO.builder()
                .id(classCourse.getId())
                .classId(classCourse.getClazz().getId())
                .className(classCourse.getClazz().getClassName())
                .courseId(classCourse.getCourse().getId())
                .courseTitle(classCourse.getCourse().getTitle())
                .assignedAt(classCourse.getAssignedAt())
                .note(classCourse.getNote())
                .build();
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new HttpBadRequest(message);
        }
        return value;
    }

    private LocalDate parseDate(String raw, String message) {
        if (raw == null || raw.isBlank()) {
            throw new HttpBadRequest(message);
        }
        try {
            return LocalDate.parse(raw);
        } catch (DateTimeParseException exception) {
            throw new HttpBadRequest(message);
        }
    }

    private LocalDate parseOptionalDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(raw);
        } catch (DateTimeParseException exception) {
            throw new HttpBadRequest("Ngày kết thúc lớp học không hợp lệ");
        }
    }

    private void validateDateRange(LocalDate start, LocalDate end) {
        if (start == null) return;
        if (end != null && end.isBefore(start)) {
            throw new HttpBadRequest("Ngày kết thúc không được trước ngày bắt đầu");
        }
    }

    private ClassStatus parseStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return ClassStatus.UPCOMING;
        }
        try {
            return ClassStatus.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new HttpBadRequest("Trạng thái lớp học không hợp lệ (UPCOMING/ONGOING/COMPLETED/CANCELLED)");
        }
    }

    private ClassEnrollmentStatus parseEnrollmentStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return ClassEnrollmentStatus.ACTIVE;
        }
        try {
            return ClassEnrollmentStatus.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new HttpBadRequest("Trạng thái học viên không hợp lệ (ACTIVE/COMPLETED/DROPPED)");
        }
    }

    private ClassTeacherRole parseTeacherRole(String raw) {
        if (raw == null || raw.isBlank()) {
            return ClassTeacherRole.INSTRUCTOR;
        }
        try {
            return ClassTeacherRole.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new HttpBadRequest("Vai trò giảng viên không hợp lệ (INSTRUCTOR/ASSISTANT)");
        }
    }

    private BigDecimal normalizeScore(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return value;
    }
}

