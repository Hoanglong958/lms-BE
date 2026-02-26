package com.ra.base_spring_boot.services.classroom.impl;

import com.ra.base_spring_boot.dto.Attendance.AttendanceRecordRequestDTO;
import com.ra.base_spring_boot.dto.Attendance.AttendanceRecordResponseDTO;
import com.ra.base_spring_boot.dto.Attendance.AttendanceSessionRequestDTO;
import com.ra.base_spring_boot.dto.Attendance.AttendanceSessionResponseDTO;
import com.ra.base_spring_boot.dto.Attendance.AttendanceClassSummaryResponseDTO;
import com.ra.base_spring_boot.dto.Attendance.AttendanceCourseSummaryResponseDTO;
import com.ra.base_spring_boot.model.Class;
import com.ra.base_spring_boot.model.ClassStudent;
import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.model.ScheduleItem;
import com.ra.base_spring_boot.model.attendance.AttendanceRecord;
import com.ra.base_spring_boot.model.attendance.AttendanceSession;
import com.ra.base_spring_boot.model.constants.AttendanceStatus;
import com.ra.base_spring_boot.model.constants.SessionStatus;
import com.ra.base_spring_boot.repository.classroom.IAttendanceRecordRepository;
import com.ra.base_spring_boot.repository.classroom.IAttendanceSessionRepository;
import com.ra.base_spring_boot.repository.classroom.IClassRepository;
import com.ra.base_spring_boot.repository.classroom.IClassStudentRepository;
import com.ra.base_spring_boot.repository.classroom.IScheduleItemRepository;
import com.ra.base_spring_boot.repository.user.IUserRepository;
import com.ra.base_spring_boot.services.classroom.IAttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        private final IScheduleItemRepository scheduleItemRepository;

        @Override
        @Transactional
        public AttendanceSessionResponseDTO createSession(AttendanceSessionRequestDTO request) {
                Objects.requireNonNull(request, "request must not be null");
                Long classId = Objects.requireNonNull(request.getClassId(), "classId must not be null");
                Class classroom = classRepo.findById(classId)
                                .orElseThrow(() -> new IllegalArgumentException("Class not found: " + classId));

                LocalDate date = LocalDate.parse(request.getSessionDate());

                // Validate session date is within class date range
                if (classroom.getStartDate() != null && date.isBefore(classroom.getStartDate())) {
                        throw new IllegalArgumentException(
                                        "Session date " + date + " is before class start date "
                                                        + classroom.getStartDate());
                }
                if (classroom.getEndDate() != null && date.isAfter(classroom.getEndDate())) {
                        throw new IllegalArgumentException(
                                        "Session date " + date + " is after class end date " + classroom.getEndDate());
                }

                // NEW: Validate that this date has a scheduled class
                boolean hasSchedule = validateScheduleExists(classId, date, request.getStartTime());
                if (!hasSchedule) {
                        throw new IllegalArgumentException(
                                        "Không thể tạo buổi điểm danh cho ngày " + date +
                                                        " vì không có lịch học vào thời gian này. Vui lòng kiểm tra lại thời khóa biểu.");
                }

                LocalTime start = request.getStartTime() != null ? LocalTime.parse(request.getStartTime()) : null;
                LocalTime end = request.getEndTime() != null ? LocalTime.parse(request.getEndTime()) : null;
                SessionStatus status = request.getStatus() != null ? SessionStatus.valueOf(request.getStatus())
                                : SessionStatus.NOT_STARTED;

                AttendanceSession session = AttendanceSession.builder()
                                .classroom(classroom)
                                .title(request.getTitle())
                                .sessionDate(date)
                                .startTime(start)
                                .endTime(end)
                                .status(status)
                                .build();
                session = sessionRepo.save(Objects.requireNonNull(session, "session must not be null"));

                return toSessionDto(session);
        }

        @Override
        @Transactional(readOnly = true)
        public List<AttendanceSessionResponseDTO> listSessionsByClass(Long classId) {
                Long safeClassId = Objects.requireNonNull(classId, "classId must not be null");
                List<AttendanceSession> list = sessionRepo.findByClassroom_IdOrderBySessionDateDesc(safeClassId);
                return list.stream().map(this::toSessionDto).collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<AttendanceRecordResponseDTO> listStudentsForSession(Long attendanceSessionId) {
                Long safeSessionId = Objects.requireNonNull(attendanceSessionId,
                                "attendanceSessionId must not be null");
                AttendanceSession session = sessionRepo.findById(safeSessionId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Attendance Session not found: " + safeSessionId));

                Long classId = session.getClassroom().getId();
                List<ClassStudent> students = classStudentRepo.findByClassroomIdWithRelations(classId);
                List<AttendanceRecord> existing = recordRepo.findBySession_Id(attendanceSessionId);
                Map<Long, AttendanceRecord> mapByStudent = existing.stream()
                                .collect(Collectors.toMap(r -> r.getStudent().getId(), r -> r));

                List<AttendanceRecordResponseDTO> result = new ArrayList<>();
                for (ClassStudent cs : students) {
                        AttendanceRecord rec = mapByStudent.get(cs.getStudent().getId());
                        if (rec == null) {
                                result.add(AttendanceRecordResponseDTO.builder()
                                                .attendanceRecordId(null)
                                                .attendanceSessionId(attendanceSessionId)
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
        @Transactional
        public AttendanceRecordResponseDTO markAttendance(AttendanceRecordRequestDTO request) {
                Objects.requireNonNull(request, "request must not be null");
                Long safeSessionId = Objects.requireNonNull(request.getAttendanceSessionId(),
                                "attendanceSessionId must not be null");
                Long safeStudentId = Objects.requireNonNull(request.getStudentId(), "studentId must not be null");
                AttendanceSession session = sessionRepo.findById(safeSessionId)
                                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + safeSessionId));
                User student = userRepo.findById(safeStudentId)
                                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + safeStudentId));

                AttendanceStatus status = request.getStatus() != null ? AttendanceStatus.valueOf(request.getStatus())
                                : AttendanceStatus.ABSENT;
                LocalDateTime checkin = request.getCheckinTime() != null ? LocalDateTime.parse(request.getCheckinTime())
                                : null;

                AttendanceRecord rec;

                // NEW: Check if this is an update (has attendanceRecordId) or create new
                if (request.getAttendanceRecordId() != null) {
                        // Update existing record
                        rec = recordRepo.findById(request.getAttendanceRecordId())
                                        .orElseThrow(() -> new IllegalArgumentException("Attendance record not found: "
                                                        + request.getAttendanceRecordId()));
                } else {
                        // ALWAYS create new record for each day - don't search for existing ones
                        // This prevents overwriting previous day's attendance
                        rec = AttendanceRecord.builder()
                                        .session(session)
                                        .student(student)
                                        .sessionDate(session.getSessionDate())
                                        .build();
                }

                rec.setStatus(status);
                rec.setCheckinTime(checkin);
                rec.setNote(request.getNote());
                rec = recordRepo.save(rec);
                return toRecordDto(rec);
        }

        @Override
        @Transactional
        public List<AttendanceRecordResponseDTO> markAttendanceBulk(Long sessionId,
                        List<AttendanceRecordRequestDTO> records) {
                Long safeSessionId = Objects.requireNonNull(sessionId, "sessionId must not be null");
                List<AttendanceRecordRequestDTO> safeRecords = Objects.requireNonNull(records,
                                "records must not be null");
                List<AttendanceRecordResponseDTO> result = new ArrayList<>();
                for (AttendanceRecordRequestDTO r : safeRecords) {
                        if (r.getAttendanceSessionId() == null)
                                r.setAttendanceSessionId(safeSessionId);
                        result.add(markAttendance(r));
                }
                return result;
        }

        @Override
        @Transactional(readOnly = true)
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
        @Transactional(readOnly = true)
        public AttendanceCourseSummaryResponseDTO summarizeByClassAndCourse(Long classId, Long courseId) {
                Long safeClassId = Objects.requireNonNull(classId, "classId must not be null");
                Long safeCourseId = Objects.requireNonNull(courseId, "courseId must not be null");
                List<AttendanceSession> sessions = sessionRepo.findByClassroom_IdAndCourse_IdOrderBySessionDateDesc(
                                safeClassId,
                                safeCourseId);
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

        /**
         * Validate that there's a schedule for the given class and date
         */
        @Override
        @Transactional(readOnly = true)
        public boolean validateScheduleForDate(Long classId, String date) {
                try {
                        LocalDate requestDate = LocalDate.parse(date);
                        List<ScheduleItem> scheduleItems = scheduleItemRepository
                                        .findByClassIdAndDate(classId, requestDate);
                        return !scheduleItems.isEmpty();
                } catch (Exception e) {
                        System.err.println("Error validating schedule date: " + e.getMessage());
                        return false;
                }
        }

        /**
         * Validate that there's a schedule for the given class and date
         */
        @Override
        @Transactional(readOnly = true)
        public List<AttendanceRecordResponseDTO> getAttendanceByClassAndDate(Long classId, String date) {
                try {
                        LocalDate requestDate = LocalDate.parse(date);
                        List<AttendanceRecord> records = recordRepo
                                        .findByClassIdAndSessionDate(classId, requestDate);
                        return records.stream()
                                        .map(this::toRecordDto)
                                        .collect(Collectors.toList());
                } catch (Exception e) {
                        System.err.println("Error getting attendance by class and date: " + e.getMessage());
                        return List.of();
                }
        }

        /**
         * Validate that there's a schedule for given class, date, and time
         */
        private boolean validateScheduleExists(Long classId, LocalDate date, String startTime) {
                try {
                        // Get schedule items for this class and specific date
                        List<ScheduleItem> scheduleItems = scheduleItemRepository
                                        .findByClassIdAndDate(classId, date);

                        if (scheduleItems.isEmpty()) {
                                return false;
                        }

                        // If no specific time provided, just check if any schedule exists for the date
                        if (startTime == null) {
                                return true;
                        }

                        // Check if there's a schedule matching both date and time
                        LocalTime requestedTime = LocalTime.parse(startTime);
                        return scheduleItems.stream()
                                        .anyMatch(item -> {
                                                // Check if the requested time matches the period time
                                                LocalTime periodStart = item.getPeriod().getStartTime();
                                                LocalTime periodEnd = item.getPeriod().getEndTime();

                                                // Allow some flexibility (within 15 minutes)
                                                return !requestedTime.isBefore(periodStart.minusMinutes(15)) &&
                                                                !requestedTime.isAfter(periodEnd.plusMinutes(15));
                                        });
                } catch (Exception e) {
                        // Log error but don't prevent session creation
                        System.err.println("Error validating schedule: " + e.getMessage());
                        return true; // Allow creation if validation fails
                }
        }

        private AttendanceSessionResponseDTO toSessionDto(AttendanceSession s) {
                long total = recordRepo.findBySession_Id(s.getId()).size();
                return AttendanceSessionResponseDTO.builder()
                                .attendanceSessionId(s.getId())
                                .classId(s.getClassroom().getId())
                                .className(s.getClassroom().getClassName())
                                .title(s.getTitle())
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
                                .attendanceRecordId(r.getId()) // NEW: Include record ID
                                .attendanceSessionId(r.getSession().getId())
                                .studentId(r.getStudent().getId())
                                .studentName(r.getStudent().getFullName())
                                .status(r.getStatus().name())
                                .checkinTime(r.getCheckinTime())
                                .note(r.getNote())
                                .build();
        }
}
