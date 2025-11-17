package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.ExamAttempt.ExamAttemptResponseDTO;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.exception.HttpNotFound;
import com.ra.base_spring_boot.model.*;
import com.ra.base_spring_boot.repository.*;
import com.ra.base_spring_boot.services.IExamAttemptService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import com.ra.base_spring_boot.repository.IExamParticipant;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamAttemptServiceImpl implements IExamAttemptService {

    private final IExamAttemptRepository attemptRepository;
    private final IExamRepository examRepository;
    private final IUserRepository userRepository;
    private final com.ra.base_spring_boot.repository.IExamAnswerRepository examAnswerRepository;
    private final ModelMapper modelMapper;
    private final IQuestionRepository questionRepository;
    private final IExamParticipant participantRepository;

    @Override
    @Transactional
    public ExamAttemptResponseDTO startAttempt(Long examId, Long userId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy bài thi với id = " + examId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy người dùng với id = " + userId));

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
    @Transactional
    public ExamAttemptResponseDTO submitAttempt(Long attemptId) {
        ExamAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy lượt làm bài với id = " + attemptId));
        
        if (attempt.getStatus() != ExamAttempt.AttemptStatus.IN_PROGRESS) {
            throw new HttpBadRequest("Lượt làm bài này đã được nộp hoặc đã được chấm điểm!");
        }
        
        attempt.setEndTime(LocalDateTime.now());
        double computedScore = calculateScore(attempt);
        attempt.setScore(computedScore);
        attempt.setStatus(ExamAttempt.AttemptStatus.GRADED);
        attemptRepository.save(attempt);
        return toDTO(attempt);
    }

    @Override
    @Transactional
    public ExamAttemptResponseDTO gradeAttempt(Long attemptId) {
        ExamAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy lượt làm bài với id = " + attemptId));

        // Recompute score using the same auto-grading logic
        double computedScore = calculateScore(attempt);
        attempt.setScore(computedScore);
        attempt.setStatus(ExamAttempt.AttemptStatus.GRADED);
        attemptRepository.save(attempt);
        return toDTO(attempt);
    }

    @Override
    public ExamAttemptResponseDTO getById(Long id) {
        ExamAttempt attempt = attemptRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy lượt làm bài với id = " + id));
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

    @Override
    @Transactional
    public ExamAttempt createAttempt(Long examId, Long userId, String examRoomId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy bài thi với id = " + examId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy người dùng với id = " + userId));

        // Lưu thông tin participant nếu chưa tồn tại
        ExamParticipant participant = participantRepository
                .findByExamIdAndUserId(examId, userId)
                .orElseGet(() -> participantRepository.save(
                        ExamParticipant.builder()
                                .examRoomId(examRoomId)
                                .exam(exam)
                                .user(user)
                                .joinTime(LocalDateTime.now())
                                .build()
                ));

        participant.setStarted(true);
        participantRepository.save(participant);

        ExamAttempt attempt = ExamAttempt.builder()
                .exam(exam)
                .user(user)
                .startTime(LocalDateTime.now())
                .status(ExamAttempt.AttemptStatus.IN_PROGRESS)
                .build();

        return attemptRepository.save(attempt);
    }

    @Override
    @Transactional
    public ExamAttempt submitExam(Long attemptId, Map<Long, String> answers) {
        ExamAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy lượt làm bài với id = " + attemptId));

        // Validate attempt status
        if (attempt.getStatus() != ExamAttempt.AttemptStatus.IN_PROGRESS 
                && attempt.getStatus() != ExamAttempt.AttemptStatus.SUBMITTED) {
            throw new HttpBadRequest("Lượt làm bài này đã được nộp và không thể chỉnh sửa!");
        }

        // Validate exam exists
        if (attempt.getExam() == null) {
            throw new HttpBadRequest("Bài thi không tồn tại!");
        }

        // Replace existing answers for idempotency
        examAnswerRepository.deleteByAttempt_Id(attemptId);

        if (answers != null && !answers.isEmpty()) {
            List<Question> questions = questionRepository.findAllById(answers.keySet());
            
            // Validate all provided question IDs exist
            Map<Long, Question> qMap = questions.stream()
                    .collect(Collectors.toMap(Question::getId, q -> q));
            
            for (Long questionId : answers.keySet()) {
                if (!qMap.containsKey(questionId)) {
                    throw new HttpBadRequest("Câu hỏi với id = " + questionId + " không tồn tại!");
                }
            }

            for (Map.Entry<Long, String> e : answers.entrySet()) {
                Question q = qMap.get(e.getKey());
                if (q == null) continue;

                String selected = e.getValue();
                String correct = q.getCorrectAnswer();
                boolean isCorrect = selected != null && correct != null
                        && selected.trim().equalsIgnoreCase(correct.trim());

                ExamAnswer ea = ExamAnswer.builder()
                        .attempt(attempt)
                        .question(q)
                        .selectedAnswer(selected)
                        .isCorrect(isCorrect)
                        .scoreAwarded(isCorrect ? 1 : 0)
                        .build();
                examAnswerRepository.save(ea);
            }
        }

        attempt.setEndTime(LocalDateTime.now());
        attempt.setStatus(ExamAttempt.AttemptStatus.SUBMITTED);

        ExamParticipant participant = participantRepository
                .findByExamIdAndUserId(attempt.getExam().getId(), attempt.getUser().getId())
                .orElseGet(() -> participantRepository.save(
                        ExamParticipant.builder()
                                .exam(attempt.getExam())
                                .user(attempt.getUser())
                                .examRoomId("AUTO-" + attempt.getExam().getId() + "-" + attempt.getUser().getId())
                                .joinTime(LocalDateTime.now())
                                .started(true)
                                .build()
                ));
        participant.setSubmitted(true);
        participantRepository.save(participant);

        return attemptRepository.save(attempt);
    }



    /**
     * Calculate score for an exam attempt
     * Formula: (correct / total) * maxScore
     */
    private double calculateScore(ExamAttempt attempt) {
        // Validate exam exists to prevent NullPointerException
        if (attempt.getExam() == null) {
            throw new HttpBadRequest("Bài thi không tồn tại!");
        }
        
        Long attemptId = attempt.getId();
        long correct = examAnswerRepository.countByAttempt_IdAndIsCorrectTrue(attemptId);
        
        // Prefer exam totalQuestions; if 0, fallback to number of answers recorded
        long totalQuestions = attempt.getExam().getTotalQuestions() != null
                ? attempt.getExam().getTotalQuestions()
                : 0;
        if (totalQuestions == 0) {
            totalQuestions = examAnswerRepository.countByAttempt_Id(attemptId);
        }

        double maxScore = attempt.getExam().getMaxScore() != null 
                ? attempt.getExam().getMaxScore() 
                : 100.0;
        
        if (totalQuestions == 0) {
            return 0.0;
        }
        
        return ((double) correct / (double) totalQuestions) * maxScore;
    }

    private ExamAttemptResponseDTO toDTO(ExamAttempt entity) {
        // Validate entity to prevent NullPointerException
        if (entity == null) {
            throw new HttpBadRequest("Entity không hợp lệ!");
        }
        if (entity.getExam() == null) {
            throw new HttpBadRequest("Bài thi không tồn tại!");
        }
        if (entity.getUser() == null) {
            throw new HttpBadRequest("Người dùng không tồn tại!");
        }
        
        ExamAttemptResponseDTO dto = modelMapper.map(entity, ExamAttemptResponseDTO.class);
        dto.setExamId(entity.getExam().getId());
        dto.setUserId(entity.getUser().getId());
        dto.setStatus(entity.getStatus() != null ? entity.getStatus().name() : "UNKNOWN");
        return dto;
    }



}
