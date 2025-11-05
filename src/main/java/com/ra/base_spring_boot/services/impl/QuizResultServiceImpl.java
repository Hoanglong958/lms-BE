package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.model.LessonQuiz;
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

@Service
@RequiredArgsConstructor
@Transactional
public class QuizResultServiceImpl implements IQuizResultService {

    private final IQuizResultRepository quizResultRepository;
    private final ILessonQuizRepository lessonQuizRepository;
    private final IUserRepository userRepository;

    /**
     * Lấy tất cả kết quả quiz (chưa bị xóa mềm)
     */
    @Override
    public List<QuizResult> findAll() {
        return quizResultRepository.findAllByDeletedFalse();
    }

    /**
     * Lấy kết quả quiz theo ID (chỉ nếu chưa bị xóa)
     */
    @Override
    public QuizResult findById(Long id) {
        return quizResultRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("QuizResult ID " + id + " not found or deleted"));
    }

    /**
     * Lưu hoặc cập nhật kết quả quiz
     */
    @Override
    public QuizResult save(QuizResult quizResult) {
        return quizResultRepository.save(quizResult);
    }

    /**
     * Xóa mềm kết quả quiz theo ID
     */
    @Override
    public void delete(Long id) {
        QuizResult result = quizResultRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("QuizResult ID " + id + " not found or already deleted"));
        result.setDeleted(true);
        quizResultRepository.save(result);
    }

    /**
     * Khôi phục kết quả quiz đã bị xóa mềm
     */
    @Override
    public void restore(Long id) {
        QuizResult result = quizResultRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("QuizResult ID " + id + " not found"));
        if (result.isDeleted()) {
            result.setDeleted(false);
            quizResultRepository.save(result);
        }
    }

    /**
     * Khi user nộp bài quiz → tính điểm & lưu kết quả
     */
    @Override
    public QuizResult submitQuiz(Long quizId, Long userId, int correctCount, int totalCount) {
        LessonQuiz quiz = lessonQuizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("LessonQuiz ID " + quizId + " not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User ID " + userId + " not found"));

        int score = (int) Math.round((correctCount * 100.0) / Math.max(totalCount, 1));
        boolean isPassed = score >= 50;

        QuizResult result = QuizResult.builder()
                .quiz(quiz)
                .user(user)
                .correctCount(correctCount)
                .totalCount(totalCount)
                .score(score)
                .isPassed(isPassed)
                .submittedAt(LocalDateTime.now())
                .deleted(false)
                .build();

        return quizResultRepository.save(result);
    }
}
