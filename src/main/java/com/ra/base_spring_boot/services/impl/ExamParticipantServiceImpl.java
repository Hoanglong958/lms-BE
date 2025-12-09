package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.Exam;
import com.ra.base_spring_boot.model.ExamParticipant;
import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.repository.IExamParticipantRepository;
import com.ra.base_spring_boot.repository.IExamRepository;
import com.ra.base_spring_boot.repository.IUserRepository;
import com.ra.base_spring_boot.services.IExamParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExamParticipantServiceImpl implements IExamParticipantService {

    private final IExamParticipantRepository participantRepository;
    private final IUserRepository userRepository;
    private final IExamRepository examRepository;

    // ============================================
    // 1️⃣ USER JOIN BÀI THI
    // ============================================
    @Override
    public ExamParticipant joinExam(Long examId, Long userId, LocalDateTime joinTime) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new HttpBadRequest("User không tồn tại"));

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new HttpBadRequest("Exam không tồn tại"));

        // Tìm xem user đã tham gia bài thi này chưa
        Optional<ExamParticipant> existed =
                participantRepository.findByUser_IdAndExam_Id(userId, examId);

        if (existed.isPresent()) {
            ExamParticipant p = existed.get();

            if (Boolean.TRUE.equals(p.getSubmitted())) {
                throw new HttpBadRequest("Bạn đã hoàn thành bài thi — không thể tham gia lại.");
            }

            return p;
        }

        // Chưa join → tạo mới
        ExamParticipant participant = ExamParticipant.builder()
                .exam(exam)
                .user(user)
                .joinTime(joinTime)
                .started(true)
                .submitted(false)
                .build();

        return participantRepository.save(participant);
    }

    // ============================================
    // 2️⃣ USER SUBMIT BÀI THI
    // ============================================
    @Override
    public ExamParticipant submitExam(Long examId, Long userId, LocalDateTime submitTime) {

        ExamParticipant participant = participantRepository
                .findByUser_IdAndExam_Id(userId, examId)
                .orElseThrow(() -> new HttpBadRequest("Bạn chưa tham gia bài thi này"));

        if (Boolean.TRUE.equals(participant.getSubmitted())) {
            throw new HttpBadRequest("Bạn đã nộp bài rồi — không thể nộp lại.");
        }

        participant.setSubmitted(true);
        participant.setSubmitTime(submitTime);

        return participantRepository.save(participant);
    }

    // ============================================
    // 3️⃣ LẤY DANH SÁCH NGƯỜI THAM GIA BÀI THI
    // ============================================
    @Override
    public List<ExamParticipant> getParticipantsByExam(Long examId) {
        return participantRepository.findAllByExam_Id(examId);
    }

    // ============================================
    // 4️⃣ LẤY TRẠNG THÁI CỦA USER
    // ============================================
    @Override
    public ExamParticipant getParticipant(Long userId, Long examId) {
        return participantRepository
                .findByUser_IdAndExam_Id(userId, examId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user tham gia bài thi"));
    }

    // ============================================
    // 5️⃣ OPTIONAL SERVICE (DÙNG NỘI BỘ)
    // ============================================
    @Override
    public Optional<ExamParticipant> findByUserIdAndExamId(Long userId, Long examId) {
        return participantRepository.findByUser_IdAndExam_Id(userId, examId);
    }
}
