// ================= ExamParticipantServiceImpl.java =================
package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.model.Exam;
import com.ra.base_spring_boot.model.ExamParticipant;
import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.repository.IExamParticipantRepository;
import com.ra.base_spring_boot.repository.IExamRepository;
import com.ra.base_spring_boot.repository.IUserRepository;
import com.ra.base_spring_boot.services.IExamParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class ExamParticipantServiceImpl implements IExamParticipantService {

    private final IExamParticipantRepository participantRepository;
    private final IUserRepository userRepository;
    private final IExamRepository examRepository;

    @Override
    public ExamParticipant joinExam(Long userId, Long examRoomId, LocalDateTime joinTime) {

        User user = userRepository.findById(Objects.requireNonNull(userId, "userId must not be null"))
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        Exam exam = examRepository.findById(Objects.requireNonNull(examRoomId, "examRoomId must not be null"))
                .orElseThrow(() -> new RuntimeException("Exam không tồn tại"));

        ExamParticipant participant = ExamParticipant.builder()
                .user(user)
                .exam(exam)
                .examRoomId(Objects.requireNonNull(examRoomId, "examRoomId must not be null"))
                .joinTime(joinTime)
                .started(true)
                .submitted(false)
                .build();

        return participantRepository.save(Objects.requireNonNull(participant, "participant must not be null"));
    }

    @Override
    public ExamParticipant submitExam(Long userId, Long examId, LocalDateTime submitTime) {

        ExamParticipant participant = participantRepository
                .findByUser_IdAndExamRoomId(
                        Objects.requireNonNull(userId, "userId must not be null"),
                        Objects.requireNonNull(examId, "examId must not be null")
                )
                .orElseThrow(() -> new RuntimeException("User chưa join phòng thi"));

        participant.setSubmitted(true);

        return participantRepository.save(Objects.requireNonNull(participant, "participant must not be null"));
    }

    @Override
    public List<ExamParticipant> getParticipantsByRoom(Long examId) {
        return participantRepository.findAllByExamRoomId(Objects.requireNonNull(examId, "examId must not be null"));
    }

    @Override
    public ExamParticipant getParticipant(Long userId, Long examRoomId) {
        return participantRepository.findByUser_IdAndExamRoomId(
                Objects.requireNonNull(userId, "userId must not be null"),
                Objects.requireNonNull(examRoomId, "examRoomId must not be null")
        )
                .orElseThrow(() -> new RuntimeException("User chưa join phòng thi"));
    }

    @Override
    public Optional<ExamParticipant> findByUserIdAndExamRoomId(Long userId, Long examRoomId) {
        return Optional.empty();
    }


}
