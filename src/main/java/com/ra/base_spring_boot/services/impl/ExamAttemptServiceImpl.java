package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.ExamAttempt.ExamAttemptResponseDTO;
import com.ra.base_spring_boot.model.Exam;
import com.ra.base_spring_boot.model.ExamAttempt;
import com.ra.base_spring_boot.model.ExamParticipant;
import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.repository.*;
import com.ra.base_spring_boot.services.IExamAttemptService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ExamAttemptServiceImpl implements IExamAttemptService {

    private final IExamAttemptRepository attemptRepository;
    private final IExamRepository examRepository;
    private final IUserRepository userRepository;
    private final IExamParticipantRepository participantRepository;
    private final ModelMapper modelMapper;

    @Override
    public ExamAttemptResponseDTO startAttempt(Long examId, Long userId) {
        Long safeExamId = Objects.requireNonNull(examId, "examId must not be null");
        Long safeUserId = Objects.requireNonNull(userId, "userId must not be null");
        Exam exam = examRepository.findById(safeExamId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));
        User user = userRepository.findById(safeUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        int nextAttemptNumber = attemptRepository
                .findTopByExam_IdAndUser_IdOrderByAttemptNumberDesc(safeExamId, safeUserId)
                .map(a -> a.getAttemptNumber() + 1)
                .orElse(1);

        ExamAttempt attempt = ExamAttempt.builder()
                .exam(exam)
                .user(user)
                .startTime(LocalDateTime.now())
                .score(0.0)
                .status(ExamAttempt.AttemptStatus.IN_PROGRESS)
                .attemptNumber(nextAttemptNumber)
                .build();

        attemptRepository.save(Objects.requireNonNull(attempt, "attempt must not be null"));
        return toDTO(attempt);
    }

    @Override
    public ExamAttempt createAttempt(Long examId, Long userId, Long examRoomId) {
        // Lưu participant nếu chưa có
        ExamParticipant participant = participantRepository
                .findByUser_IdAndExamRoomId(Objects.requireNonNull(userId, "userId must not be null"), Objects.requireNonNull(examRoomId, "examRoomId must not be null"))
                .orElseGet(() -> participantRepository.save(
                        java.util.Objects.requireNonNull(
                                ExamParticipant.builder()
                                        .user(userRepository.findById(Objects.requireNonNull(userId, "userId must not be null")).orElseThrow(() -> new RuntimeException("User not found")))
                                        .exam(examRepository.findById(Objects.requireNonNull(examId, "examId must not be null")).orElseThrow(() -> new RuntimeException("Exam not found")))
                                        .examRoomId(Objects.requireNonNull(examRoomId, "examRoomId must not be null"))
                                        .joinTime(LocalDateTime.now())
                                        .started(true)
                                        .submitted(false)
                                        .build(),
                                "participant must not be null"
                        )
                ));

        participant.setStarted(true);
        participantRepository.save(Objects.requireNonNull(participant, "participant must not be null"));

        // Tạo attempt mới
        ExamAttempt attempt = ExamAttempt.builder()
                .exam(participant.getExam())
                .user(participant.getUser())
                .startTime(LocalDateTime.now())
                .status(ExamAttempt.AttemptStatus.IN_PROGRESS)
                .build();

        return attemptRepository.save(Objects.requireNonNull(attempt, "attempt must not be null"));
    }

    @Override
    public ExamAttempt submitExam(Long attemptId, Map<Long, String> answers) {
        Long safeAttemptId = Objects.requireNonNull(attemptId, "attemptId must not be null");
        ExamAttempt attempt = attemptRepository.findById(safeAttemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        attempt.setEndTime(LocalDateTime.now());
        attempt.setStatus(ExamAttempt.AttemptStatus.SUBMITTED);

        // Cập nhật trạng thái participant nếu tồn tại (REST flow có thể không có examRoomId)
        participantRepository
                .findByUser_IdAndExamRoomId(attempt.getUser().getId(), attempt.getExam().getId())
                .ifPresent(p -> {
                    p.setSubmitted(true);
                    participantRepository.save(p);
                });

        return attemptRepository.save(attempt);
    }

    @Override
    public ExamAttemptResponseDTO submitAttempt(Long attemptId) {
        ExamAttempt attempt = attemptRepository.findById(Objects.requireNonNull(attemptId, "attemptId must not be null"))
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        attempt.setEndTime(LocalDateTime.now());
        attempt.setStatus(ExamAttempt.AttemptStatus.GRADED);

        attemptRepository.save(Objects.requireNonNull(attempt, "attempt must not be null"));
        return toDTO(attempt);
    }

    @Override
    public ExamAttemptResponseDTO gradeAttempt(Long attemptId) {
        ExamAttempt attempt = attemptRepository.findById(Objects.requireNonNull(attemptId, "attemptId must not be null"))
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        // Tính điểm nếu cần
        attempt.setStatus(ExamAttempt.AttemptStatus.GRADED);
        attemptRepository.save(Objects.requireNonNull(attempt, "attempt must not be null"));

        return toDTO(attempt);
    }

    @Override
    public ExamAttemptResponseDTO getById(Long id) {
        return toDTO(attemptRepository.findById(Objects.requireNonNull(id, "id must not be null")).orElseThrow(() -> new RuntimeException("Attempt not found")));
    }

    private ExamAttemptResponseDTO toDTO(ExamAttempt entity) {
        ExamAttemptResponseDTO dto = modelMapper.map(entity, ExamAttemptResponseDTO.class);
        dto.setExamId(entity.getExam().getId());
        dto.setUserId(entity.getUser().getId());
        dto.setStatus(entity.getStatus().name());
        return dto;
    }
    @Override
    public List<ExamAttemptResponseDTO> getAll() {
        return attemptRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public List<ExamAttemptResponseDTO> getByExam(Long examId) {
        return attemptRepository.findByExam_Id(Objects.requireNonNull(examId, "examId must not be null"))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public List<ExamAttemptResponseDTO> getByUser(Long userId) {
        return attemptRepository.findByUser_Id(Objects.requireNonNull(userId, "userId must not be null"))
                .stream()
                .map(this::toDTO)
                .toList();
    }

}
