package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.dto.Attendance.*;
import com.ra.base_spring_boot.services.IAttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "30 - Attendance", description = "API điểm danh lớp học")
public class AttendanceController {

    private final IAttendanceService attendanceService;

    @Operation(summary = "Tạo buổi học để điểm danh")
    @PostMapping("/attendance-sessions")
    public ResponseEntity<AttendanceSessionResponseDTO> createSession(@RequestBody AttendanceSessionRequestDTO req) {
        return ResponseEntity.ok(attendanceService.createSession(req));
    }

    @Operation(summary = "Danh sách buổi học của lớp")
    @GetMapping("/classes/{classId}/attendance-sessions")
    public ResponseEntity<List<AttendanceSessionResponseDTO>> listSessions(@PathVariable Long classId) {
        return ResponseEntity.ok(attendanceService.listSessionsByClass(classId));
    }

    @Operation(summary = "Danh sách học viên cần điểm danh theo buổi")
    @GetMapping("/attendance-sessions/{sessionId}/students")
    public ResponseEntity<List<AttendanceRecordResponseDTO>> listStudentsForSession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(attendanceService.listStudentsForSession(sessionId));
    }

    @Operation(summary = "Điểm danh từng học viên")
    @PostMapping("/attendance")
    public ResponseEntity<AttendanceRecordResponseDTO> markAttendance(@RequestBody AttendanceRecordRequestDTO req) {
        return ResponseEntity.ok(attendanceService.markAttendance(req));
    }

    @Operation(summary = "Điểm danh hàng loạt")
    @PostMapping("/attendance/bulk")
    public ResponseEntity<List<AttendanceRecordResponseDTO>> markAttendanceBulk(@RequestBody AttendanceBulkRequestDTO req) {
        return ResponseEntity.ok(attendanceService.markAttendanceBulk(req.getSessionId(), req.getRecords()));
    }

    @Operation(summary = "Thống kê điểm danh theo từng buổi của lớp")
    @GetMapping("/classes/{classId}/attendance-summary")
    public ResponseEntity<AttendanceClassSummaryResponseDTO> summarizeByClass(@PathVariable Long classId) {
        return ResponseEntity.ok(attendanceService.summarizeByClass(classId));
    }

    @Operation(summary = "Thống kê điểm danh theo khóa học của lớp")
    @GetMapping("/classes/{classId}/courses/{courseId}/attendance")
    public ResponseEntity<AttendanceCourseSummaryResponseDTO> summarizeByCourse(
            @PathVariable Long classId,
            @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(attendanceService.summarizeByClassAndCourse(classId, courseId));
    }
}
