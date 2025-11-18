package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.model.ExamParticipant;
import com.ra.base_spring_boot.services.IExamParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exam-participant")
public class ExamParticipantController {

    private final IExamParticipantService examParticipantService;

    // User join phòng thi (REST test)
    @PostMapping("/join")
    public ResponseEntity<?> joinExam(@RequestParam Long userId,
                                      @RequestParam Long examRoomId) {
        ExamParticipant participant = examParticipantService.joinExam(userId, examRoomId, LocalDateTime.now());
        return ResponseEntity.ok(participant);
    }

    // User submit bài (REST test)
    @PostMapping("/submit")
    public ResponseEntity<?> submitExam(@RequestParam Long userId,
                                        @RequestParam Long examRoomId) {
        // ✅ Gọi submitExam đúng 3 tham số
        ExamParticipant participant = examParticipantService.submitExam(
                userId,
                examRoomId,
                LocalDateTime.now()
        );

        return ResponseEntity.ok(participant);
    }

    // Lấy trạng thái user trong phòng thi
    @GetMapping("/status")
    public ResponseEntity<?> getStatus(@RequestParam Long examRoomId) {
        return ResponseEntity.ok(examParticipantService.getParticipantsByRoom(examRoomId));
    }
}
