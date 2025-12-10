package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.Attendance.AttendanceRecordRequestDTO;
import com.ra.base_spring_boot.dto.Attendance.AttendanceRecordResponseDTO;
import com.ra.base_spring_boot.dto.Attendance.AttendanceSessionRequestDTO;
import com.ra.base_spring_boot.dto.Attendance.AttendanceSessionResponseDTO;
import com.ra.base_spring_boot.dto.Attendance.AttendanceClassSummaryResponseDTO;
import com.ra.base_spring_boot.dto.Attendance.AttendanceCourseSummaryResponseDTO;
import com.ra.base_spring_boot.model.Class;
import com.ra.base_spring_boot.model.ClassStudent;
import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.model.attendance.AttendanceRecord;
import com.ra.base_spring_boot.model.attendance.AttendanceSession;
import com.ra.base_spring_boot.model.constants.AttendanceStatus;
import com.ra.base_spring_boot.model.constants.SessionStatus;
import com.ra.base_spring_boot.repository.*;
import com.ra.base_spring_boot.services.IAttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements IAttendanceService {

    private final IAttendanceSessionRepository sessionRepo;
    private final IAttendanceRecordRepository recordRepo;
    private final IClassRepository classRepo;
    private final IUserRepository userRepo;
    private final IClassStudentRepository classStudentRepo;

    @Override
    public AttendanceSessionResponseDTO createSession(AttendanceSessionRequestDTO request) {
        Objects.requireNonNull(request, "request must not be null");
        Long classId = Objects.requireNonNull(request.getClassId(), "classId must not be null");
        Class classroom = classRepo.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("Class not found: " + classId));

        LocalDate date = LocalDate.parse(request.getSessionDate());
        LocalTime start = request.getStartTime() != null ? LocalTime.parse(request.getStartTime()) : null;
        LocalTime end = request.getEndTime() != null ? LocalTime.parse(request.getEndTime()) : null;
        SessionStatus status = request.getStatus() != null ? SessionStatus.valueOf(request.getStatus()) : SessionStatus.NOT_STARTED;

        AttendanceSession session = AttendanceSession.builder()
                .classroom(classroom)
                .course(classroom.getCourse())
                .sessionDate(date)
                .startTime(start)
                .endTime(end)
                .status(status)
                .build();
        session = sessionRepo.save(Objects.requireNonNull(session, "session must not be null"));

        return toSessionDto(session);
    }

    @Override
    public List<AttendanceSessionResponseDTO> listSessionsByClass(Long classId) {
        Long safeClassId = Objects.requireNonNull(classId, "classId must not be null");
        List<AttendanceSession> list = sessionRepo.findByClassroom_IdOrderBySessionDateDesc(safeClassId);
        return list.stream().map(this::toSessionDto).collect(Collectors.toList());
    }

    @Override
    public List<AttendanceRecordResponseDTO> listStudentsForSession(Long sessionId) {
        Long safeSessionId = Objects.requireNonNull(sessionId, "sessionId must not be null");
        AttendanceSession session = sessionRepo.findById(safeSessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + safeSessionId));

        Long classId = session.getClassroom().getId();
        List<ClassStudent> students = classStudentRepo.findByClassroomId(classId);
        List<AttendanceRecord> existing = recordRepo.findBySession_Id(sessionId);
        Map<Long, AttendanceRecord> mapByStudent = existing.stream().collect(Collectors.toMap(r -> r.getStudent().getId(), r -> r));

        List<AttendanceRecordResponseDTO> result = new ArrayList<>();
        for (ClassStudent cs : students) {
            AttendanceRecord rec = mapByStudent.get(cs.getStudent().getId());
            if (rec == null) {
                result.add(AttendanceRecordResponseDTO.builder()
                        .attendanceRecordId(null)
                        .attendanceSessionId(sessionId)
                        .studentId(cs.getStudent().getId())
                        .studentName(cs.getStudent().getFullName())
                        .status(AttendanceStatus.ABSENT.name())
                        .checkinTime(null)
                        .note(null)
                        .build());
            } else {
                result.add(toRecordDto(rec));
            }
        }
        return result;
    }

    @Override
    public AttendanceRecordResponseDTO markAttendance(AttendanceRecordRequestDTO request) {
        Objects.requireNonNull(request, "request must not be null");
        Long safeSessionId = Objects.requireNonNull(request.getAttendanceSessionId(), "attendanceSessionId must not be null");
        Long safeStudentId = Objects.requireNonNull(request.getStudentId(), "studentId must not be null");
        AttendanceSession session = sessionRepo.findById(safeSessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + safeSessionId));
        User student = userRepo.findById(safeStudentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + safeStudentId));

        AttendanceStatus status = request.getStatus() != null ? AttendanceStatus.valueOf(request.getStatus()) : AttendanceStatus.ABSENT;
        LocalDateTime checkin = request.getCheckinTime() != null ? LocalDateTime.parse(request.getCheckinTime()) : null;

        Optional<AttendanceRecord> opt = recordRepo.findBySession_IdAndStudent_Id(session.getId(), student.getId());
        AttendanceRecord rec = opt.orElseGet(() -> AttendanceRecord.builder()
                .session(session)
                .student(student)
                .build());
        rec.setStatus(status);
        rec.setCheckinTime(checkin);
        rec.setNote(request.getNote());
        rec = recordRepo.save(rec);
        return toRecordDto(rec);
    }

    @Override
    public List<AttendanceRecordResponseDTO> markAttendanceBulk(Long sessionId, List<AttendanceRecordRequestDTO> records) {
        Long safeSessionId = Objects.requireNonNull(sessionId, "sessionId must not be null");
        List<AttendanceRecordRequestDTO> safeRecords = Objects.requireNonNull(records, "records must not be null");
        List<AttendanceRecordResponseDTO> result = new ArrayList<>();
        for (AttendanceRecordRequestDTO r : safeRecords) {
            if (r.getAttendanceSessionId() == null) r.setAttendanceSessionId(safeSessionId);
            result.add(markAttendance(r));
        }
        return result;
    }

    @Override
    public AttendanceClassSummaryResponseDTO summarizeByClass(Long classId) {
        Long safeClassId = Objects.requireNonNull(classId, "classId must not be null");
        List<AttendanceSession> sessions = sessionRepo.findByClassroom_IdOrderBySessionDateDesc(safeClassId);
        List<AttendanceClassSummaryResponseDTO.SessionSummary> items = new ArrayList<>();
        for (AttendanceSession s : sessions) {
            long present = recordRepo.countBySession_IdAndStatus(s.getId(), AttendanceStatus.PRESENT);
            long late = recordRepo.countBySession_IdAndStatus(s.getId(), AttendanceStatus.LATE);
            long absent = recordRepo.countBySession_IdAndStatus(s.getId(), AttendanceStatus.ABSENT);
            long excused = recordRepo.countBySession_IdAndStatus(s.getId(), AttendanceStatus.EXCUSED);
            items.add(AttendanceClassSummaryResponseDTO.SessionSummary.builder()
                    .date(s.getSessionDate().toString())
                    .present(present)
                    .late(late)
                    .absent(absent)
                    .excused(excused)
                    .build());
        }
        return AttendanceClassSummaryResponseDTO.builder()
                .classId(safeClassId)
                .totalSessions((long) sessions.size())
                .sessions(items)
                .build();
    }

    @Override
    public AttendanceCourseSummaryResponseDTO summarizeByClassAndCourse(Long classId, Long courseId) {
        Long safeClassId = Objects.requireNonNull(classId, "classId must not be null");
        Long safeCourseId = Objects.requireNonNull(courseId, "courseId must not be null");
        List<AttendanceSession> sessions = sessionRepo.findByClassroom_IdAndCourse_IdOrderBySessionDateDesc(safeClassId, safeCourseId);
        List<AttendanceCourseSummaryResponseDTO.Item> items = new ArrayList<>();
        for (AttendanceSession s : sessions) {
            long present = recordRepo.countBySession_IdAndStatus(s.getId(), AttendanceStatus.PRESENT);
            long absent = recordRepo.countBySession_IdAndStatus(s.getId(), AttendanceStatus.ABSENT);
            long late = recordRepo.countBySession_IdAndStatus(s.getId(), AttendanceStatus.LATE);
            long excused = recordRepo.countBySession_IdAndStatus(s.getId(), AttendanceStatus.EXCUSED);
            items.add(AttendanceCourseSummaryResponseDTO.Item.builder()
                    .sessionId(s.getId())
                    .date(s.getSessionDate().toString())
                    .present(present)
                    .absent(absent)
                    .late(late)
                    .excused(excused)
                    .build());
        }
        return AttendanceCourseSummaryResponseDTO.builder()
                .classId(safeClassId)
                .courseId(safeCourseId)
                .totalSessions((long) sessions.size())
                .attendance(items)
                .build();
    }

    private AttendanceSessionResponseDTO toSessionDto(AttendanceSession s) {
        long total = recordRepo.findBySession_Id(s.getId()).size();
        return AttendanceSessionResponseDTO.builder()
                .attendanceSessionId(s.getId())
                .classId(s.getClassroom().getId())
                .className(s.getClassroom().getClassName())
                .title(null)
                .sessionDate(s.getSessionDate())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .status(s.getStatus().name())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .totalRecords(total)
                .build();
    }

    private AttendanceRecordResponseDTO toRecordDto(AttendanceRecord r) {
        return AttendanceRecordResponseDTO.builder()
                .attendanceRecordId(r.getId())
                .attendanceSessionId(r.getSession().getId())
                .studentId(r.getStudent().getId())
                .studentName(r.getStudent().getFullName())
                .status(r.getStatus().name())
                .checkinTime(r.getCheckinTime())
                .note(r.getNote())
                .build();
    }
}
