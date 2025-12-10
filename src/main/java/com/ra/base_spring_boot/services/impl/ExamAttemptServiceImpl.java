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

@Service
@RequiredArgsConstructor
public class ExamAttemptServiceImpl implements IExamAttemptService {

    private final IExamAttemptRepository attemptRepository;
    private final IExamRepository examRepository;
    private final IUserRepository userRepository;
    private final IExamParticipantRepository participantRepository;
    private final ModelMapper modelMapper;

    // -----------------------------------------------------
    @Override
    public ExamAttemptResponseDTO startAttempt(Long examId, Long userId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Tìm số lần attempt tiếp theo
        int nextAttemptNumber = attemptRepository
                .findTopByExam_IdAndUser_IdOrderByAttemptNumberDesc(examId, userId)
                .map(a -> a.getAttemptNumber() + 1)
                .orElse(1);

        // Nếu chưa join => auto join
        participantRepository.findByUser_IdAndExam_Id(userId, examId)
                .orElseGet(() -> participantRepository.save(
                        ExamParticipant.builder()
                                .exam(exam)
                                .user(user)
                                .joinTime(LocalDateTime.now())
                                .started(true)
                                .submitted(false)
                                .build()
                ));

        ExamAttempt attempt = ExamAttempt.builder()
                .exam(exam)
                .user(user)
                .startTime(LocalDateTime.now())
                .attemptNumber(nextAttemptNumber)
                .score(0.0)
                .status(ExamAttempt.AttemptStatus.IN_PROGRESS)
                .build();

        attemptRepository.save(attempt);
        return toDTO(attempt);
    }

    // -----------------------------------------------------
    @Override
    public ExamAttempt submitExam(Long attemptId, Map<Long, String> answers) {
        ExamAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        attempt.setEndTime(LocalDateTime.now());
        attempt.setStatus(ExamAttempt.AttemptStatus.SUBMITTED);

        // Update participant
        ExamParticipant participant = participantRepository
                .findByUser_IdAndExam_Id(attempt.getUser().getId(), attempt.getExam().getId())
                .orElseThrow(() -> new RuntimeException("Participant not found"));

        participant.setSubmitted(true);
        participantRepository.save(participant);

        return attemptRepository.save(attempt);
    }

    // -----------------------------------------------------
    public ExamAttempt createAttempt(Long examId, Long userId, Long ignored) {

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        participantRepository.findByUser_IdAndExam_Id(userId, examId)
                .orElseGet(() -> participantRepository.save(
                        ExamParticipant.builder()
                                .exam(exam)
                                .user(user)
                                .joinTime(LocalDateTime.now())
                                .started(true)
                                .submitted(false)
                                .build()
                ));

        ExamAttempt attempt = ExamAttempt.builder()
                .exam(exam)
                .user(user)
                .status(ExamAttempt.AttemptStatus.IN_PROGRESS)
                .startTime(LocalDateTime.now())
                .build();

        return attemptRepository.save(attempt);
    }

    // -----------------------------------------------------
    @Override
    public ExamAttemptResponseDTO submitAttempt(Long attemptId) {
        ExamAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        attempt.setEndTime(LocalDateTime.now());
        attempt.setStatus(ExamAttempt.AttemptStatus.GRADED);

        return toDTO(attemptRepository.save(attempt));
    }

    // -----------------------------------------------------
    @Override
    public ExamAttemptResponseDTO gradeAttempt(Long attemptId) {
        ExamAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        attempt.setStatus(ExamAttempt.AttemptStatus.GRADED);
        return toDTO(attemptRepository.save(attempt));
    }

    // -----------------------------------------------------
    @Override
    public ExamAttemptResponseDTO getById(Long id) {
        return toDTO(attemptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attempt not found")));
    }

    // -----------------------------------------------------
    @Override
    public List<ExamAttemptResponseDTO> getAll() {
        return attemptRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public List<ExamAttemptResponseDTO> getByExam(Long examId) {
        return attemptRepository.findByExam_Id(examId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public List<ExamAttemptResponseDTO> getByUser(Long userId) {
        return attemptRepository.findByUser_Id(userId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // -----------------------------------------------------
    private ExamAttemptResponseDTO toDTO(ExamAttempt entity) {
        ExamAttemptResponseDTO dto = modelMapper.map(entity, ExamAttemptResponseDTO.class);
        dto.setExamId(entity.getExam().getId());
        dto.setUserId(entity.getUser().getId());
        dto.setStatus(entity.getStatus().name());
        return dto;
    }
}
