package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.QuizResult.QuizResultResponseDTO;
import com.ra.base_spring_boot.dto.QuizResult.QuizSubmissionRequestDTO;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.LessonQuiz;
import com.ra.base_spring_boot.model.Question;
import com.ra.base_spring_boot.model.QuizQuestion;
import com.ra.base_spring_boot.model.QuizResult;
import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.repository.ILessonQuizRepository;
import com.ra.base_spring_boot.repository.IQuizResultRepository;
import com.ra.base_spring_boot.repository.IUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizResultServiceImplTest {

    @Mock
    private IQuizResultRepository quizResultRepository;
    @Mock
    private ILessonQuizRepository lessonQuizRepository;
    @Mock
    private IUserRepository userRepository;

    @InjectMocks
    private QuizResultServiceImpl quizResultService;

    @Test
    void submitQuiz_shouldCalculateScoreAndPersist() {
        LessonQuiz quiz = LessonQuiz.builder()
                .id(1L)
                .maxScore(100)
                .passingScore(80)
                .build();
        QuizQuestion qq1 = QuizQuestion.builder()
                .question(Question.builder().id(10L).correctAnswer("A").build())
                .build();
        QuizQuestion qq2 = QuizQuestion.builder()
                .question(Question.builder().id(20L).correctAnswer("B").build())
                .build();
        quiz.setQuizQuestions(List.of(qq1, qq2));

        User user = User.builder().id(2L).build();

        when(lessonQuizRepository.findById(1L)).thenReturn(Optional.of(quiz));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(quizResultRepository.save(any(QuizResult.class))).thenAnswer(invocation -> {
            QuizResult entity = invocation.getArgument(0);
            entity.setId(99L);
            return entity;
        });

        QuizSubmissionRequestDTO request = QuizSubmissionRequestDTO.builder()
                .quizId(1L)
                .userId(2L)
                .answers(List.of(
                        QuizSubmissionRequestDTO.AnswerItem.builder().questionId(10L).answer("A").build(),
                        QuizSubmissionRequestDTO.AnswerItem.builder().questionId(20L).answer("C").build()
                ))
                .build();

        QuizResultResponseDTO response = quizResultService.submitQuiz(request);

        assertThat(response.getId()).isEqualTo(99L);
        assertThat(response.getCorrectCount()).isEqualTo(1);
        assertThat(response.getTotalCount()).isEqualTo(2);
        assertThat(response.getScore()).isEqualTo(50); // 1/2 of maxScore 100
        assertThat(response.getIsPassed()).isFalse(); // passing score 80

        ArgumentCaptor<QuizResult> captor = ArgumentCaptor.forClass(QuizResult.class);
        verify(quizResultRepository).save(captor.capture());
        QuizResult saved = captor.getValue();
        assertThat(saved.getSubmittedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(saved.isDeleted()).isFalse();
    }

    @Test
    void submitQuiz_shouldThrowWhenQuizHasNoQuestions() {
        LessonQuiz quiz = LessonQuiz.builder().id(1L).build();
        quiz.setQuizQuestions(List.of());

        when(lessonQuizRepository.findById(1L)).thenReturn(Optional.of(quiz));
        when(userRepository.findById(2L)).thenReturn(Optional.of(User.builder().id(2L).build()));

        QuizSubmissionRequestDTO request = QuizSubmissionRequestDTO.builder()
                .quizId(1L)
                .userId(2L)
                .answers(List.of(
                        QuizSubmissionRequestDTO.AnswerItem.builder().questionId(10L).answer("A").build()
                ))
                .build();

        assertThrows(HttpBadRequest.class, () -> quizResultService.submitQuiz(request));
        verify(quizResultRepository, never()).save(any());
    }
}

