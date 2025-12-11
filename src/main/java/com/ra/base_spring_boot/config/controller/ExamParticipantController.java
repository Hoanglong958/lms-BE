package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.model.ExamParticipant;
import com.ra.base_spring_boot.services.IExamParticipantService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/v1/exam-participants")
@RequiredArgsConstructor
@RequestMapping("/api/exam-participant")
@Tag(name = "31 - Exam Participant", description = "Quản lý tham gia thi")
public class ExamParticipantController {

    private final IExamParticipantService examParticipantService;

    // ==========================================
    // 1️⃣ User JOIN bài thi
    // ==========================================
    @PostMapping("/join")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @Operation(summary = "User tham gia kỳ thi")
    @ApiResponse(responseCode = "200", description = "Join thành công")
    public ResponseEntity<ExamParticipant> joinExam(
            @Parameter(description = "ID bài thi") @RequestParam Long examId,
            @Parameter(description = "ID người dùng") @RequestParam Long userId
    ) {
        ExamParticipant participant =
                examParticipantService.joinExam(examId, userId, LocalDateTime.now());

        return ResponseEntity.ok(participant);
    }

    // ==========================================
    // 2️⃣ User SUBMIT bài thi
    // ==========================================
    @PostMapping("/submit")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @Operation(summary = "User nộp bài thi")
    @ApiResponse(responseCode = "200", description = "Submit thành công")
    public ResponseEntity<ExamParticipant> submitExam(
            @Parameter(description = "ID bài thi") @RequestParam Long examId,
            @Parameter(description = "ID người dùng") @RequestParam Long userId
    ) {
        ExamParticipant participant =
                examParticipantService.submitExam(examId, userId, LocalDateTime.now());

        return ResponseEntity.ok(participant);
    }

    // ==========================================
    // 3️⃣ Lấy danh sách người tham gia bài thi (ADMIN)
    // ==========================================
    @GetMapping("/exam-status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Lấy danh sách người tham gia kỳ thi")
    @ApiResponse(responseCode = "200", description = "Thành công")
    public ResponseEntity<List<ExamParticipant>> getExamStatus(
            @Parameter(description = "ID bài thi") @RequestParam Long examId
    ) {
        return ResponseEntity.ok(examParticipantService.getParticipantsByExam(examId));
    }
}
