package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.ExamAttempt.ExamAttemptResponseDTO;
import com.ra.base_spring_boot.model.Exam;
import com.ra.base_spring_boot.model.ExamAttempt;
import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.repository.IExamAttemptRepository;
import com.ra.base_spring_boot.repository.IExamRepository;
import com.ra.base_spring_boot.repository.IUserRepository;
import com.ra.base_spring_boot.repository.IExamAnswerRepository;
import com.ra.base_spring_boot.services.IExamAttemptService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamAttemptServiceImpl implements IExamAttemptService {

    private final IExamAttemptRepository attemptRepository;
    private final IExamRepository examRepository;
    private final IUserRepository userRepository;
    private final com.ra.base_spring_boot.repository.IExamAnswerRepository examAnswerRepository;
    private final ModelMapper modelMapper;

    @Override
    public ExamAttemptResponseDTO startAttempt(Long examId, Long userId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        int nextAttemptNumber = attemptRepository
                .findTopByExam_IdAndUser_IdOrderByAttemptNumberDesc(examId, userId)
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

        attemptRepository.save(attempt);
        return toDTO(attempt);
    }

    @Override
    public ExamAttemptResponseDTO submitAttempt(Long attemptId) {
        ExamAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));
        attempt.setEndTime(LocalDateTime.now());

        // Auto grade: score = (correct / total) * maxScore
        long correct = examAnswerRepository.countByAttempt_IdAndIsCorrectTrue(attemptId);
        // Prefer exam totalQuestions; if 0, fallback to number of answers recorded
        long totalQuestions = attempt.getExam().getTotalQuestions() != null
                ? attempt.getExam().getTotalQuestions()
                : 0;
        if (totalQuestions == 0) {
            totalQuestions = examAnswerRepository.countByAttempt_Id(attemptId);
        }

        double maxScore = attempt.getExam().getMaxScore() != null ? attempt.getExam().getMaxScore() : 100.0;
        double computedScore = 0.0;
        if (totalQuestions > 0) {
            computedScore = ((double) correct / (double) totalQuestions) * maxScore;
        }

        attempt.setScore(computedScore);
        attempt.setStatus(ExamAttempt.AttemptStatus.GRADED);
        attemptRepository.save(attempt);
        return toDTO(attempt);
    }

    @Override
    public ExamAttemptResponseDTO gradeAttempt(Long attemptId) {
        ExamAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        // Recompute score using the same auto-grading logic
        long correct = examAnswerRepository.countByAttempt_IdAndIsCorrectTrue(attemptId);
        long totalQuestions = attempt.getExam().getTotalQuestions() != null
                ? attempt.getExam().getTotalQuestions()
                : 0;
        if (totalQuestions == 0) {
            totalQuestions = examAnswerRepository.countByAttempt_Id(attemptId);
        }

        double maxScore = attempt.getExam().getMaxScore() != null ? attempt.getExam().getMaxScore() : 100.0;
        double computedScore = 0.0;
        if (totalQuestions > 0) {
            computedScore = ((double) correct / (double) totalQuestions) * maxScore;
        }

        attempt.setScore(computedScore);
        attempt.setStatus(ExamAttempt.AttemptStatus.GRADED);
        attemptRepository.save(attempt);
        return toDTO(attempt);
    }

    @Override
    public ExamAttemptResponseDTO getById(Long id) {
        ExamAttempt attempt = attemptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));
        return toDTO(attempt);
    }

    @Override
    public List<ExamAttemptResponseDTO> getAll() {
        return attemptRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<ExamAttemptResponseDTO> getByExam(Long examId) {
        return attemptRepository.findByExam_Id(examId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<ExamAttemptResponseDTO> getByUser(Long userId) {
        return attemptRepository.findByUser_Id(userId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    private ExamAttemptResponseDTO toDTO(ExamAttempt entity) {
        ExamAttemptResponseDTO dto = modelMapper.map(entity, ExamAttemptResponseDTO.class);
        dto.setExamId(entity.getExam().getId());
        dto.setUserId(entity.getUser().getId());
        dto.setStatus(entity.getStatus().name());
        return dto;
    }
}
