package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.ExamAttempt.ExamAnswerDTO;

import java.util.List;

public interface IExamAnswerService {
    /**
     * Lấy danh sách đáp án theo attempt.
     *
     * @param attemptId       id lượt làm bài
     * @param onlyCurrentUser nếu true: chỉ cho phép user xem đáp án của chính lượt làm của mình
     */
    List<ExamAnswerDTO> getByAttempt(Long attemptId, boolean onlyCurrentUser);
}


