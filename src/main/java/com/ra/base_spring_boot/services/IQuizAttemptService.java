package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.Quiz.QuizAttemptResponse;
import com.ra.base_spring_boot.dto.Quiz.StartAttemptRequest;
import com.ra.base_spring_boot.dto.Quiz.SubmitAttemptRequest;

import java.util.List;

public interface IQuizAttemptService {
    QuizAttemptResponse start(StartAttemptRequest request);

    QuizAttemptResponse submit(Long attemptId, SubmitAttemptRequest request);

    QuizAttemptResponse get(Long attemptId);

    List<QuizAttemptResponse> byUser(Long userId);

    List<QuizAttemptResponse> byQuiz(Long quizId);

    List<QuizAttemptResponse> byUserAndQuiz(Long userId, Long quizId);
}
