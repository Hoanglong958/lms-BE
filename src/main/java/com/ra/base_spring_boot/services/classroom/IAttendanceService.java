package com.ra.base_spring_boot.services.classroom;

import com.ra.base_spring_boot.dto.Attendance.AttendanceRecordRequestDTO;
import com.ra.base_spring_boot.dto.Attendance.AttendanceRecordResponseDTO;
import com.ra.base_spring_boot.dto.Attendance.AttendanceSessionRequestDTO;
import com.ra.base_spring_boot.dto.Attendance.AttendanceSessionResponseDTO;
import com.ra.base_spring_boot.dto.Attendance.AttendanceClassSummaryResponseDTO;
import com.ra.base_spring_boot.dto.Attendance.AttendanceCourseSummaryResponseDTO;

import java.util.List;

public interface IAttendanceService {
    AttendanceSessionResponseDTO createSession(AttendanceSessionRequestDTO request);

    List<AttendanceSessionResponseDTO> listSessionsByClass(Long classId);

    List<AttendanceRecordResponseDTO> listStudentsForSession(Long attendanceSessionId);

    AttendanceRecordResponseDTO markAttendance(AttendanceRecordRequestDTO request);

    List<AttendanceRecordResponseDTO> markAttendanceBulk(Long sessionId, List<AttendanceRecordRequestDTO> records);

    AttendanceClassSummaryResponseDTO summarizeByClass(Long classId);

    AttendanceCourseSummaryResponseDTO summarizeByClassAndCourse(Long classId, Long courseId);

    /**
     * Kiểm tra xem một ngày có lịch học không
     */
    boolean validateScheduleForDate(Long classId, String date);

    /**
     * Lấy điểm danh theo lớp và ngày
     */
    List<AttendanceRecordResponseDTO> getAttendanceByClassAndDate(Long classId, String date);
}
