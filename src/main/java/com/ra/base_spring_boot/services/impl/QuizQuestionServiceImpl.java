package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.Quiz.CreateQuizQuestionRequest;
import com.ra.base_spring_boot.dto.Quiz.QuizQuestionDTO;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.exception.HttpNotFound;
import com.ra.base_spring_boot.model.LessonQuiz;
import com.ra.base_spring_boot.model.Question;
import com.ra.base_spring_boot.model.QuizQuestion;
import com.ra.base_spring_boot.repository.ILessonQuizRepository;
import com.ra.base_spring_boot.repository.IQuestionRepository;
import com.ra.base_spring_boot.repository.IQuizQuestionRepository;
import com.ra.base_spring_boot.services.IQuizQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizQuestionServiceImpl implements IQuizQuestionService {

    private final IQuizQuestionRepository quizQuestionRepository;
    private final ILessonQuizRepository lessonQuizRepository;
    private final IQuestionRepository questionRepository;

    @Override
    public List<QuizQuestionDTO> getByQuiz(Long quizId) {
        List<QuizQuestion> items = quizQuestionRepository.findByQuiz_IdOrderByOrderIndexAscIdAsc(java.util.Objects.requireNonNull(quizId, "quizId must not be null"));
        return items.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public QuizQuestionDTO create(CreateQuizQuestionRequest request) {
        LessonQuiz quiz = lessonQuizRepository.findById(java.util.Objects.requireNonNull(request.getQuizId(), "quizId must not be null"))
                .orElseThrow(() -> new HttpNotFound("Quiz không tồn tại"));
        Question question = questionRepository.findById(java.util.Objects.requireNonNull(request.getQuestionId(), "questionId must not be null"))
                .orElseThrow(() -> new HttpNotFound("Câu hỏi không tồn tại"));

        if (quizQuestionRepository.existsByQuiz_IdAndQuestion_Id(quiz.getId(), question.getId())) {
            throw new HttpBadRequest("Câu hỏi đã tồn tại trong quiz");
        }

        Integer orderIndex = request.getOrderIndex();
        if (orderIndex == null) {
            long count = quizQuestionRepository.countByQuiz_Id(quiz.getId());
            orderIndex = (int) count + 1;
        }

        QuizQuestion entity = QuizQuestion.builder()
                .quiz(quiz)
                .question(question)
                .orderIndex(orderIndex)
                .build();

        entity = quizQuestionRepository.save(java.util.Objects.requireNonNull(entity, "quizQuestion must not be null"));

        // update questionCount on quiz
        Integer qc = quiz.getQuestionCount() == null ? 0 : quiz.getQuestionCount();
        quiz.setQuestionCount(qc + 1);
        lessonQuizRepository.save(java.util.Objects.requireNonNull(quiz, "quiz must not be null"));

        return toDTO(entity);
    }

    @Override
    public void delete(Long id) {
        QuizQuestion entity = quizQuestionRepository.findById(java.util.Objects.requireNonNull(id, "id must not be null"))
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy bản ghi quiz_question"));
        LessonQuiz quiz = entity.getQuiz();
        quizQuestionRepository.delete(java.util.Objects.requireNonNull(entity, "quizQuestion must not be null"));

        if (quiz != null) {
            Integer qc = quiz.getQuestionCount() == null ? 0 : quiz.getQuestionCount();
            quiz.setQuestionCount(Math.max(0, qc - 1));
            lessonQuizRepository.save(quiz);
        }
    }

    private QuizQuestionDTO toDTO(QuizQuestion entity) {
        return QuizQuestionDTO.builder()
                .id(entity.getId())
                .quizId(entity.getQuiz() != null ? entity.getQuiz().getId() : null)
                .questionId(entity.getQuestion() != null ? entity.getQuestion().getId() : null)
                .orderIndex(entity.getOrderIndex())
                .build();
    }
}
