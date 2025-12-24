package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.Quiz.QuizAttemptResponse;
import com.ra.base_spring_boot.dto.Quiz.StartAttemptRequest;
import com.ra.base_spring_boot.dto.Quiz.SubmitAttemptRequest;
import com.ra.base_spring_boot.model.LessonQuiz;
import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.model.quiz.AttemptStatus;
import com.ra.base_spring_boot.model.quiz.QuizAttempt;
import com.ra.base_spring_boot.repository.ILessonQuizRepository;
import com.ra.base_spring_boot.repository.IUserRepository;
import com.ra.base_spring_boot.repository.quiz.IQuizAttemptRepository;
import com.ra.base_spring_boot.services.IQuizAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizAttemptServiceImpl implements IQuizAttemptService {

    private final IQuizAttemptRepository attemptRepo;
    private final ILessonQuizRepository quizRepo;
    private final IUserRepository userRepo;

    @Override
    public QuizAttemptResponse start(StartAttemptRequest request) {
        LessonQuiz quiz = quizRepo
                .findById(java.util.Objects.requireNonNull(request.getQuizId(), "quizId must not be null"))
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found: " + request.getQuizId()));
        User user = userRepo.findById(java.util.Objects.requireNonNull(request.getUserId(), "userId must not be null"))
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.getUserId()));

        int last = attemptRepo.maxAttemptNumberByQuizAndUser(quiz.getId(), user.getId());
        QuizAttempt attempt = QuizAttempt.builder()
                .quiz(quiz)
                .user(user)
                .attemptNumber(last + 1)
                .status(AttemptStatus.IN_PROGRESS)
                .startTime(LocalDateTime.now())
                .build();
        attempt = attemptRepo.save(java.util.Objects.requireNonNull(attempt, "attempt must not be null"));
        return toDto(attempt);
    }

    @Override
    public QuizAttemptResponse submit(Long attemptId, SubmitAttemptRequest request) {
        QuizAttempt attempt = attemptRepo
                .findById(java.util.Objects.requireNonNull(attemptId, "attemptId must not be null"))
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found: " + attemptId));
        attempt.setEndTime(LocalDateTime.now());
        attempt.finalizeTiming();
        if (request.getScore() != null)
            attempt.setScore(request.getScore());
        if (request.getCorrectCount() != null)
            attempt.setCorrectCount(request.getCorrectCount());
        if (request.getTotalCount() != null)
            attempt.setTotalCount(request.getTotalCount());
        if (request.getPassed() != null)
            attempt.setPassed(request.getPassed());
        attempt.setStatus(AttemptStatus.SUBMITTED);
        attempt = attemptRepo.save(java.util.Objects.requireNonNull(attempt, "attempt must not be null"));
        return toDto(attempt);
    }

    @Override
    @Transactional(readOnly = true)
    public QuizAttemptResponse get(Long attemptId) {
        return attemptRepo.findById(java.util.Objects.requireNonNull(attemptId, "attemptId must not be null"))
                .map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found: " + attemptId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizAttemptResponse> byUser(Long userId) {
        return attemptRepo
                .findByUser_IdOrderByCreatedAtDesc(java.util.Objects.requireNonNull(userId, "userId must not be null"))
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizAttemptResponse> byQuiz(Long quizId) {
        return attemptRepo
                .findByQuiz_IdOrderByCreatedAtDesc(java.util.Objects.requireNonNull(quizId, "quizId must not be null"))
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizAttemptResponse> byUserAndQuiz(Long userId, Long quizId) {
        return attemptRepo.findByUser_IdAndQuiz_IdOrderByCreatedAtDesc(
                java.util.Objects.requireNonNull(userId, "userId must not be null"),
                java.util.Objects.requireNonNull(quizId, "quizId must not be null")).stream().map(this::toDto)
                .collect(Collectors.toList());
    }

    private QuizAttemptResponse toDto(QuizAttempt a) {
        return QuizAttemptResponse.builder()
                .attemptId(a.getId())
                .quizId(a.getQuiz().getId())
                .quizTitle(a.getQuiz().getTitle())
                .userId(a.getUser().getId())
                .startTime(a.getStartTime())
                .endTime(a.getEndTime())
                .score(a.getScore())
                .correctCount(a.getCorrectCount())
                .totalCount(a.getTotalCount())
                .passed(a.getPassed())
                .status(a.getStatus().name())
                .attemptNumber(a.getAttemptNumber())
                .timeSpentSeconds(a.getTimeSpentSeconds())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
