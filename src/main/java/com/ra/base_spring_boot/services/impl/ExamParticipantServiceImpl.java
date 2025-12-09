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

@Service
@RequiredArgsConstructor
public class ExamParticipantServiceImpl implements IExamParticipantService {

    private final IExamParticipantRepository participantRepository;
    private final IUserRepository userRepository;
    private final IExamRepository examRepository;

    @Override
    public ExamParticipant joinExam(Long userId, Long examRoomId, LocalDateTime joinTime) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        Exam exam = examRepository.findById(examRoomId)
                .orElseThrow(() -> new RuntimeException("Exam không tồn tại"));

        // ====== CHECK 1 — đã join rồi thì trả lại participant cũ ======
        Optional<ExamParticipant> existed = participantRepository
                .findByUser_IdAndExamRoomId(userId, examRoomId);

        if (existed.isPresent()) {
            ExamParticipant p = existed.get();

            // ====== CHECK 2 — nếu đã submit thì không cho thi lại ======
            if (p.getSubmitted() != null && p.getSubmitted()) {
                throw new RuntimeException("Bạn đã hoàn thành bài thi. Không thể tham gia lại.");
            }

            // ====== CHECK 3 — nếu đang thi dở thì trả lại trạng thái cũ (fix lỗi refresh) ======
            return p;
        }

        // ====== CHƯA TỪNG JOIN → tạo participant mới ======
        ExamParticipant participant = ExamParticipant.builder()
                .user(user)
                .exam(exam)
                .examRoomId(examRoomId)
                .joinTime(joinTime)
                .started(true)
                .submitted(false)
                .build();

        return participantRepository.save(participant);
    }

    @Override
    public ExamParticipant submitExam(Long userId, Long examId, LocalDateTime submitTime) {

        ExamParticipant participant = participantRepository
                .findByUser_IdAndExamRoomId(userId, examId)
                .orElseThrow(() -> new RuntimeException("User chưa join phòng thi"));

        if (participant.getSubmitted() != null && participant.getSubmitted()) {
            throw new RuntimeException("Bạn đã nộp bài rồi. Không thể nộp lại.");
        }

        participant.setSubmitted(true);
        participant.setSubmitTime(submitTime);

        return participantRepository.save(participant);
    }

    @Override
    public List<ExamParticipant> getParticipantsByRoom(Long examId) {
        return participantRepository.findAllByExamRoomId(examId);
    }

    @Override
    public ExamParticipant getParticipant(Long userId, Long examRoomId) {
        return participantRepository.findByUser_IdAndExamRoomId(userId, examRoomId)
                .orElseThrow(() -> new RuntimeException("User chưa join phòng thi"));
    }

    @Override
    public Optional<ExamParticipant> findByUserIdAndExamRoomId(Long userId, Long examRoomId) {
        return participantRepository.findByUser_IdAndExamRoomId(userId, examRoomId);
    }
}
