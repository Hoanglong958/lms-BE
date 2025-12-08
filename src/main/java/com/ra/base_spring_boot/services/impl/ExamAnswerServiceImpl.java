package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.ExamAttempt.ExamAnswerDTO;
import com.ra.base_spring_boot.exception.HttpNotFound;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.ExamAnswer;
import com.ra.base_spring_boot.model.ExamAttempt;
import com.ra.base_spring_boot.repository.IExamAttemptRepository;
import com.ra.base_spring_boot.security.principle.MyUserDetails;
import com.ra.base_spring_boot.services.IExamAnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamAnswerServiceImpl implements IExamAnswerService {

    private final IExamAttemptRepository examAttemptRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ExamAnswerDTO> getByAttempt(Long attemptId, boolean onlyCurrentUser) {
        ExamAttempt attempt = examAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy lượt làm bài với id = " + attemptId));

        if (onlyCurrentUser) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new HttpBadRequest("Bạn chưa đăng nhập!");
            }
            Object principal = authentication.getPrincipal();
            if (!(principal instanceof MyUserDetails myUserDetails)) {
                throw new HttpBadRequest("Thông tin người dùng không hợp lệ!");
            }
            Long currentUserId = myUserDetails.getUser().getId();
            if (!attempt.getUser().getId().equals(currentUserId)) {
                throw new HttpBadRequest("Bạn không có quyền xem đáp án của lượt làm này!");
            }
        }

        return attempt.getAnswers()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private ExamAnswerDTO toDto(ExamAnswer entity) {
        return ExamAnswerDTO.builder()
                .id(entity.getId())
                .attemptId(entity.getAttempt() != null ? entity.getAttempt().getId() : null)
                .questionId(entity.getQuestion() != null ? entity.getQuestion().getId() : null)
                .selectedAnswer(entity.getSelectedAnswer())
                .isCorrect(entity.getIsCorrect())
                .scoreAwarded(entity.getScoreAwarded())
                .build();
    }
}


