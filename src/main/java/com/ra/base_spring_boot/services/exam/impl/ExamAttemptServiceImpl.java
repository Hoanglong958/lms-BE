package com.ra.base_spring_boot.services.exam.impl;

import com.ra.base_spring_boot.dto.ExamAttempt.ExamAttemptResponseDTO;
import com.ra.base_spring_boot.model.*;
import com.ra.base_spring_boot.repository.exam.IExamAnswerRepository;
import com.ra.base_spring_boot.repository.exam.IExamAttemptRepository;
import com.ra.base_spring_boot.repository.exam.IExamParticipantRepository;
import com.ra.base_spring_boot.repository.exam.IExamRepository;
import com.ra.base_spring_boot.repository.exam.IQuestionRepository;
import com.ra.base_spring_boot.repository.user.IUserRepository;
import com.ra.base_spring_boot.services.exam.IExamAttemptService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.ra.base_spring_boot.exception.HttpForbiden;
import com.ra.base_spring_boot.model.constants.RoleName;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExamAttemptServiceImpl implements IExamAttemptService {

    private final IExamAttemptRepository attemptRepository;
    private final IExamRepository examRepository;
    private final IUserRepository userRepository;
    private final IExamParticipantRepository participantRepository;
    private final IQuestionRepository questionRepository;
    private final IExamAnswerRepository examAnswerRepository;
    private final ModelMapper modelMapper;

    private static final int SEARCH_LIMIT = 20;
    // =====================================================================
    @Override
    @Transactional
    public ExamAttemptResponseDTO startAttempt(Long examId, Long userId) {

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == RoleName.ROLE_USER) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = exam.getStartTime();
            LocalDateTime end = exam.getEndTime();
            if (start != null && now.isBefore(start)) {
                throw new HttpForbiden("Bài kiểm tra chưa đến thời gian làm.");
            }
            if (end != null && now.isAfter(end)) {
                throw new HttpForbiden("Bài kiểm tra đã kết thúc.");
            }
        }

        // 🔥 Nếu đã có attempt đang làm thì trả về luôn
        Optional<ExamAttempt> existingAttempt = attemptRepository.findTopByExam_IdAndUser_IdAndStatus(
                examId, userId, ExamAttempt.AttemptStatus.IN_PROGRESS);

        if (existingAttempt.isPresent()) {
            return toDTO(existingAttempt.get());
        }

        // Nếu chưa join → tạo participant
        participantRepository.findByUser_IdAndExam_Id(userId, examId)
                .orElseGet(() -> participantRepository.save(
                        ExamParticipant.builder()
                                .exam(exam)
                                .user(user)
                                .joinTime(LocalDateTime.now())
                                .started(true)
                                .submitted(false)
                                .build()));

        // Tính attempt number
        int nextAttempt = attemptRepository
                .findTopByExam_IdAndUser_IdOrderByAttemptNumberDesc(examId, userId)
                .map(a -> a.getAttemptNumber() + 1)
                .orElse(1);

        // 🔥 Tạo attempt mới
        ExamAttempt attempt = ExamAttempt.builder()
                .exam(exam)
                .user(user)
                .startTime(LocalDateTime.now())
                .attemptNumber(nextAttempt)
                .score(0.0)
                .status(ExamAttempt.AttemptStatus.IN_PROGRESS)
                .build();

        // Thay thế: Kiểm tra null thủ công (nhưng attempt ở đây chắc chắn không null,
        // có thể xóa luôn)
        if (attempt == null) {
            throw new IllegalArgumentException("attempt must not be null");
        }
        attemptRepository.save(attempt);
        return toDTO(attempt);
    }

    // =====================================================================
    @Override
    @Transactional
    public ExamAttemptResponseDTO submitExam(Long attemptId, Map<Long, String> answers) {

        ExamAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        Exam exam = attempt.getExam();
        User user = attempt.getUser();
        if (user != null && user.getRole() == RoleName.ROLE_USER) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime end = exam.getEndTime();
            if (end != null && now.isAfter(end)) {
                throw new HttpForbiden("Bài kiểm tra đã kết thúc.");
            }
        }

        // Tính điểm mỗi câu
        int totalQuestions = exam.getExamQuestions().size();
        double maxScore = exam.getMaxScore();
        double pointPerQuestion = maxScore / totalQuestions;

        double totalScore = 0;

        // Xóa câu trả lời cũ nếu có
        examAnswerRepository.deleteByAttempt_Id(attemptId);

        // Chấm điểm
        for (Map.Entry<Long, String> entry : answers.entrySet()) {

            Long questionId = entry.getKey();
            String userAnswer = entry.getValue();

            Question question = questionRepository.findById(questionId)
                    .orElseThrow(() -> new RuntimeException("Question not found: " + questionId));

            boolean correct = question.getCorrectAnswer().equalsIgnoreCase(userAnswer);
            int awarded = correct ? (int) pointPerQuestion : 0;

            if (correct)
                totalScore += pointPerQuestion;

            examAnswerRepository.save(
                    ExamAnswer.builder()
                            .attempt(attempt)
                            .question(question)
                            .selectedAnswer(userAnswer)
                            .isCorrect(correct)
                            .scoreAwarded(awarded)
                            .build());
        }

        // Cập nhật attempt
        attempt.setEndTime(LocalDateTime.now());
        attempt.setScore(totalScore);
        attempt.setStatus(ExamAttempt.AttemptStatus.GRADED);
        attemptRepository.save(attempt);

        // Update participant
        ExamParticipant participant = participantRepository
                .findByUser_IdAndExam_Id(attempt.getUser().getId(), exam.getId())
                .orElseThrow(() -> new RuntimeException("Participant not found"));

        participant.setSubmitted(true);
        participantRepository.save(participant);

        return toDTO(attempt);
    }

    // =====================================================================
    @Override
    @Transactional
    public ExamAttemptResponseDTO submitAttempt(Long attemptId) {
        // Thay thế: Kiểm tra null thủ công
        if (attemptId == null) {
            throw new IllegalArgumentException("attemptId must not be null");
        }
        ExamAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        attempt.setEndTime(LocalDateTime.now());
        attempt.setStatus(ExamAttempt.AttemptStatus.GRADED);

        return toDTO(attemptRepository.save(attempt));
    }

    // =====================================================================
    @Override
    @Transactional
    public ExamAttemptResponseDTO gradeAttempt(Long attemptId) {
        // Thay thế: Kiểm tra null thủ công
        if (attemptId == null) {
            throw new IllegalArgumentException("attemptId must not be null");
        }
        ExamAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        attempt.setStatus(ExamAttempt.AttemptStatus.GRADED);
        return toDTO(attemptRepository.save(attempt));
    }

    // =====================================================================
    @Override
    public ExamAttemptResponseDTO getById(Long id) {
        return toDTO(attemptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attempt not found")));
    }

    // =====================================================================
    @Override
    public List<ExamAttemptResponseDTO> getAll() {
        return mapToDTO(attemptRepository.findAll());
    }

    // =====================================================================
    @Override
    public List<ExamAttemptResponseDTO> getByExam(Long examId) {
        if (examId == null) {
            throw new IllegalArgumentException("examId must not be null");
        }
        return mapToDTO(attemptRepository.findByExam_Id(examId));
    }

    // =====================================================================
    @Override
    public List<ExamAttemptResponseDTO> getByUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        return mapToDTO(attemptRepository.findByUser_Id(userId));
    }

    @Override
    public List<ExamAttemptResponseDTO> search(Long userId, String keyword) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        String term = keyword != null ? keyword.trim() : "";
        List<ExamAttempt> attempts;
        if (StringUtils.hasText(term)) {
            attempts = attemptRepository.findTop20ByUser_IdAndExam_TitleContainingIgnoreCaseOrderByStartTimeDesc(userId, term);
        } else {
            attempts = attemptRepository.findTop20ByUser_IdOrderByStartTimeDesc(userId);
        }
        return mapToDTO(attempts);
    }

    // =====================================================================
    @Override
    @Transactional
    public void delete(Long id) {
        if (id == null)
            throw new IllegalArgumentException("id must not be null");
        ExamAttempt attempt = attemptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));
        attemptRepository.delete(attempt);
    }

    // =====================================================================
    @Override
    @Transactional
    public ExamAttemptResponseDTO updateStatus(Long id, ExamAttempt.AttemptStatus status) {
        if (id == null)
            throw new IllegalArgumentException("id must not be null");
        if (status == null)
            throw new IllegalArgumentException("status must not be null");
        ExamAttempt attempt = attemptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));
        attempt.setStatus(status);
        return toDTO(attemptRepository.save(attempt));
    }

    // =====================================================================
    private ExamAttemptResponseDTO toDTO(ExamAttempt entity) {
        ExamAttemptResponseDTO dto = modelMapper.map(entity, ExamAttemptResponseDTO.class);
        if (entity.getExam() != null) {
            dto.setExamId(entity.getExam().getId());
            dto.setExamTitle(entity.getExam().getTitle());
        }
        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getId());
        }
        dto.setStatus(entity.getStatus().name());
        return dto;
    }

    private List<ExamAttemptResponseDTO> mapToDTO(List<ExamAttempt> attempts) {
        return attempts.stream().map(this::toDTO).toList();
    }
}
