package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.QuizResult.QuizResultResponseDTO;
import com.ra.base_spring_boot.dto.QuizResult.QuizSubmissionRequestDTO;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.exception.HttpNotFound;
import com.ra.base_spring_boot.model.LessonQuiz;
import com.ra.base_spring_boot.model.QuizQuestion;
import com.ra.base_spring_boot.model.QuizResult;
import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.repository.ILessonQuizRepository;
import com.ra.base_spring_boot.repository.IQuizResultRepository;
import com.ra.base_spring_boot.repository.IUserRepository;
import com.ra.base_spring_boot.services.IQuizResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizResultServiceImpl implements IQuizResultService {

    private final IQuizResultRepository quizResultRepository;
    private final ILessonQuizRepository lessonQuizRepository;
    private final IUserRepository userRepository;

    @Override
    public List<QuizResultResponseDTO> findAll() {
        return quizResultRepository.findAllByDeletedFalse()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public QuizResultResponseDTO findById(Long id) {
        QuizResult result = quizResultRepository.findByIdAndDeletedFalse(java.util.Objects.requireNonNull(id, "id must not be null"))
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy kết quả quiz với id = " + id));
        return toDTO(result);
    }

    @Override
    public QuizResult save(QuizResult quizResult) {
        return quizResultRepository.save(java.util.Objects.requireNonNull(quizResult, "quizResult must not be null"));
    }

    @Override
    public void delete(Long id) {
        QuizResult result = quizResultRepository.findByIdAndDeletedFalse(java.util.Objects.requireNonNull(id, "id must not be null"))
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy kết quả quiz với id = " + id));
        result.setDeleted(true);
        quizResultRepository.save(java.util.Objects.requireNonNull(result, "quizResult must not be null"));
    }

    @Override
    public void restore(Long id) {
        QuizResult result = quizResultRepository.findById(java.util.Objects.requireNonNull(id, "id must not be null"))
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy kết quả quiz với id = " + id));
        if (result.isDeleted()) {
            result.setDeleted(false);
            quizResultRepository.save(java.util.Objects.requireNonNull(result, "quizResult must not be null"));
        }
    }

    @Override
    public QuizResultResponseDTO submitQuiz(QuizSubmissionRequestDTO request) {
        LessonQuiz quiz = lessonQuizRepository.findById(java.util.Objects.requireNonNull(request.getQuizId(), "quizId must not be null"))
                .orElseThrow(() -> new HttpNotFound("Quiz không tồn tại"));
        User user = userRepository.findById(java.util.Objects.requireNonNull(request.getUserId(), "userId must not be null"))
                .orElseThrow(() -> new HttpNotFound("User không tồn tại"));

        Map<Long, String> submittedAnswers = request.getAnswers()
                .stream()
                .collect(Collectors.toMap(
                        QuizSubmissionRequestDTO.AnswerItem::getQuestionId,
                        QuizSubmissionRequestDTO.AnswerItem::getAnswer,
                        (existing, replacement) -> replacement
                ));

        List<QuizQuestion> quizQuestions = quiz.getQuizQuestions();
        if (quizQuestions == null || quizQuestions.isEmpty()) {
            throw new HttpBadRequest("Quiz hiện chưa có câu hỏi để chấm điểm");
        }

        int correctCount = 0;
        for (QuizQuestion quizQuestion : quizQuestions) {
            if (quizQuestion.getQuestion() == null) continue;
            Long questionId = quizQuestion.getQuestion().getId();
            String providedAnswer = submittedAnswers.get(questionId);
            String correctAnswer = quizQuestion.getQuestion().getCorrectAnswer();
            boolean isCorrect = providedAnswer != null
                    && correctAnswer != null
                    && providedAnswer.trim().equalsIgnoreCase(correctAnswer.trim());
            if (isCorrect) {
                correctCount++;
            }
        }

        int totalCount = quizQuestions.size();
        int maxScore = quiz.getMaxScore() != null ? quiz.getMaxScore() : 100;
        // Thống nhất công thức tính điểm với ExamAttempt: (correct / total) * maxScore
        // Quiz sử dụng int và làm tròn, Exam sử dụng double - giữ nguyên vì phù hợp với business logic
        int score = totalCount > 0
                ? (int) Math.round(((double) correctCount / (double) totalCount) * maxScore)
                : 0;

        int passingScore = quiz.getPassingScore() != null ? quiz.getPassingScore() : 50;
        boolean isPassed = score >= passingScore;

        QuizResult result = QuizResult.builder()
                .quiz(quiz)
                .user(user)
                .correctCount(correctCount)
                .totalCount(totalCount)
                .score(score)
                .isPassed(isPassed)
                .passScore(passingScore)
                .submittedAt(LocalDateTime.now())
                .deleted(false)
                .build();

        result = quizResultRepository.save(java.util.Objects.requireNonNull(result, "quizResult must not be null"));
        return toDTO(result);
    }

    private QuizResultResponseDTO toDTO(QuizResult entity) {
        QuizResultResponseDTO dto = new QuizResultResponseDTO();
        dto.setId(entity.getId());
        dto.setQuizId(entity.getQuiz().getId());
        dto.setUserId(entity.getUser().getId());
        dto.setCorrectCount(entity.getCorrectCount());
        dto.setTotalCount(entity.getTotalCount());
        dto.setScore(entity.getScore());
        dto.setIsPassed(entity.getIsPassed());
        dto.setSubmittedAt(entity.getSubmittedAt());
        return dto;
    }
}
