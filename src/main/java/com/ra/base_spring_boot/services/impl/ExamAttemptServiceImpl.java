package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.ExamAttempt.ExamAttemptResponseDTO;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.Exam;
import com.ra.base_spring_boot.model.ExamAttempt;
import com.ra.base_spring_boot.model.ExamParticipant;
import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.repository.IExamAttemptRepository;
import com.ra.base_spring_boot.repository.IExamParticipantRepository;
import com.ra.base_spring_boot.repository.IExamRepository;
import com.ra.base_spring_boot.repository.IUserRepository;
import com.ra.base_spring_boot.services.IExamAttemptService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExamAttemptServiceImpl implements IExamAttemptService {

    private final IExamAttemptRepository attemptRepository;
    private final IExamRepository examRepository;
    private final IUserRepository userRepository;
    private final IExamParticipantRepository participantRepository;
    private final ModelMapper modelMapper;

    // ==========================
    // START EXAM ATTEMPT
    // ==========================
    @Override
    public ExamAttemptResponseDTO startAttempt(Long examId, Long userId) {

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new HttpBadRequest("Exam not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new HttpBadRequest("User not found"));

        // Nếu user đã submit → không thi lại
        participantRepository.findByUser_IdAndExam_Id(userId, examId)
                .ifPresent(p -> {
                    if (Boolean.TRUE.equals(p.getSubmitted()))
                        throw new HttpBadRequest("Bạn đã hoàn thành bài thi — không thể làm lại");
                });

        // Đảm bảo participant tồn tại
        ExamParticipant participant = participantRepository
                .findByUser_IdAndExam_Id(userId, examId)
                .orElseGet(() -> participantRepository.save(
                        ExamParticipant.builder()
                                .exam(exam)
                                .user(user)
                                .joinTime(LocalDateTime.now())
                                .started(true)
                                .submitted(false)
                                .build()
                ));

        participant.setStarted(true);
        participantRepository.save(participant);

        // Lần attempt tiếp theo
        int nextNumber = attemptRepository
                .findTopByExam_IdAndUser_IdOrderByAttemptNumberDesc(examId, userId)
                .map(a -> a.getAttemptNumber() + 1)
                .orElse(1);

        ExamAttempt attempt = ExamAttempt.builder()
                .exam(exam)
                .user(user)
                .attemptNumber(nextNumber)
                .startTime(LocalDateTime.now())
                .status(ExamAttempt.AttemptStatus.IN_PROGRESS)
                .score(0.0)
                .build();

        attemptRepository.save(attempt);

        return toDTO(attempt);
    }

    // ==========================
    // SUBMIT EXAM — cập nhật điểm + trả kết quả
    // ==========================
    @Override
    public ExamAttempt submitExam(Long attemptId, Map<Long, String> answers) {

        ExamAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new HttpBadRequest("Attempt not found"));

        attempt.setEndTime(LocalDateTime.now());
        attempt.setStatus(ExamAttempt.AttemptStatus.SUBMITTED);

        // Update participant (không có examRoomId nữa)
        ExamParticipant participant = participantRepository
                .findByUser_IdAndExam_Id(attempt.getUser().getId(), attempt.getExam().getId())
                .orElseThrow(() -> new HttpBadRequest("ExamParticipant not found"));

        participant.setSubmitted(true);
        participantRepository.save(participant);

        // TODO: Tính điểm từ answers
        // attempt.setScore(score);

        return attemptRepository.save(attempt);
    }

    // ==========================
    // TRẢ VỀ KẾT QUẢ BÀI THI
    // ==========================
    @Override
    public ExamAttemptResponseDTO submitAttempt(Long attemptId) {

        ExamAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new HttpBadRequest("Attempt not found"));

        attempt.setEndTime(LocalDateTime.now());
        attempt.setStatus(ExamAttempt.AttemptStatus.GRADED);

        attemptRepository.save(attempt);

        return toDTO(attempt);
    }

    @Override
    public ExamAttemptResponseDTO gradeAttempt(Long attemptId) {

        ExamAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new HttpBadRequest("Attempt not found"));

        attempt.setStatus(ExamAttempt.AttemptStatus.GRADED);

        attemptRepository.save(attempt);

        return toDTO(attempt);
    }

    @Override
    public ExamAttemptResponseDTO getById(Long id) {
        return toDTO(
                attemptRepository.findById(id)
                        .orElseThrow(() -> new HttpBadRequest("Attempt not found"))
        );
    }

    // ==========================
    // MAPPING
    // ==========================
    private ExamAttemptResponseDTO toDTO(ExamAttempt entity) {
        ExamAttemptResponseDTO dto = modelMapper.map(entity, ExamAttemptResponseDTO.class);
        dto.setExamId(entity.getExam().getId());
        dto.setUserId(entity.getUser().getId());
        dto.setStatus(entity.getStatus().name());
        return dto;
    }

    @Override
    public List<ExamAttemptResponseDTO> getAll() {
        return attemptRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Override
    public List<ExamAttemptResponseDTO> getByExam(Long examId) {
        return attemptRepository.findByExam_Id(examId).stream().map(this::toDTO).toList();
    }

    @Override
    public List<ExamAttemptResponseDTO> getByUser(Long userId) {
        return attemptRepository.findByUser_Id(userId).stream().map(this::toDTO).toList();
    }
}
