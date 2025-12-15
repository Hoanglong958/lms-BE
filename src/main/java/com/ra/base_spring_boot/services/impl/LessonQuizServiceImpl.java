package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.LessonQuizzes.LessonQuizRequestDTO;
import com.ra.base_spring_boot.dto.LessonQuizzes.LessonQuizResponseDTO;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.Lesson;
import com.ra.base_spring_boot.model.LessonQuiz;
import com.ra.base_spring_boot.model.constants.LessonType;
import com.ra.base_spring_boot.repository.ILessonQuizRepository;
import com.ra.base_spring_boot.repository.ILessonRepository;
import com.ra.base_spring_boot.services.ILessonQuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LessonQuizServiceImpl implements ILessonQuizService {

    private final ILessonQuizRepository lessonQuizRepository;
    private final ILessonRepository lessonRepository;

    /**
     * Tạo quiz mới cho bài học
     */
    @Override
    public LessonQuizResponseDTO create(LessonQuizRequestDTO request) {
        Lesson lesson = lessonRepository.findById(java.util.Objects.requireNonNull(request.getLessonId(), "lessonId must not be null"))
                .orElseThrow(() -> new HttpBadRequest("Lesson not found"));

        // Validate lesson type must be QUIZ
        if (lesson.getType() != LessonType.QUIZ) {
            throw new HttpBadRequest("Lesson type must be QUIZ để tạo quiz cho bài học này");
        }

        LessonQuiz quiz = LessonQuiz.builder()
                .lesson(lesson)
                .title(request.getTitle())
                .questionCount(request.getQuestionCount())
                .maxScore(request.getMaxScore())
                .passingScore(request.getPassingScore())
                .build();

        lessonQuizRepository.save(java.util.Objects.requireNonNull(quiz, "quiz must not be null"));
        return toResponse(quiz);
    }

    /**
     * Lấy thông tin quiz theo ID
     */
    @Override
    public LessonQuizResponseDTO getById(Long id) {
        LessonQuiz quiz = lessonQuizRepository.findById(java.util.Objects.requireNonNull(id, "id must not be null"))
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        return toResponse(quiz);
    }

    /**
     * Lấy danh sách quiz theo bài học
     */
    @Override
    public List<LessonQuizResponseDTO> getByLesson(Long lessonId) {
        return lessonQuizRepository.findByLesson_Id(java.util.Objects.requireNonNull(lessonId, "lessonId must not be null"))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Cập nhật quiz
     */
    @Override
    public LessonQuizResponseDTO update(Long id, LessonQuizRequestDTO request) {
        LessonQuiz quiz = lessonQuizRepository.findById(java.util.Objects.requireNonNull(id, "id must not be null"))
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        quiz.setTitle(request.getTitle());
        quiz.setQuestionCount(request.getQuestionCount());
        quiz.setMaxScore(request.getMaxScore());
        quiz.setPassingScore(request.getPassingScore());

        lessonQuizRepository.save(quiz);
        return toResponse(quiz);
    }

    /**
     * Xóa quiz
     */
    @Override
    public void delete(Long id) {
        if (!lessonQuizRepository.existsById(java.util.Objects.requireNonNull(id, "id must not be null"))) {
            throw new RuntimeException("Quiz not found");
        }
        lessonQuizRepository.deleteById(id);
    }

    /**
     * Chuyển đổi entity → response DTO
     */
    private LessonQuizResponseDTO toResponse(LessonQuiz quiz) {
        return LessonQuizResponseDTO.builder()
                .quizId(quiz.getId())
                .lessonId(quiz.getLesson().getId())
                .lessonTitle(quiz.getLesson().getTitle())
                .title(quiz.getTitle())
                .questionCount(quiz.getQuestionCount())
                .maxScore(quiz.getMaxScore())
                .passingScore(quiz.getPassingScore())
                .build();
    }
}
