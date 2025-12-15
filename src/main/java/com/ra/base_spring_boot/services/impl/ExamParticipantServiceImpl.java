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
    public ExamParticipant joinExam(Long examId, Long userId, LocalDateTime joinTime) {
        // Thay thế: Kiểm tra null thủ công cho params
        if (examId == null) {
            throw new IllegalArgumentException("examId must not be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam không tồn tại"));

        // Nếu đã tham gia rồi thì trả về luôn
        Optional<ExamParticipant> exists = participantRepository
                .findByUser_IdAndExam_Id(userId, examId);

        if (exists.isPresent()) {
            return exists.get();
        }

        ExamParticipant participant = ExamParticipant.builder()
                .user(user)
                .exam(exam)
                .joinTime(joinTime != null ? joinTime : LocalDateTime.now())
                .started(true)
                .submitted(false)
                .build();

        // Thay thế: Kiểm tra null thủ công (participant ở đây chắc chắn không null, có thể xóa nếu muốn)
        if (participant == null) {
            throw new IllegalArgumentException("participant must not be null");
        }
        return participantRepository.save(participant);
    }

    @Override
    public ExamParticipant submitExam(Long examId, Long userId, LocalDateTime submitTime) {
        // Thay thế: Kiểm tra null thủ công cho params
        if (examId == null) {
            throw new IllegalArgumentException("examId must not be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }

        ExamParticipant participant = participantRepository
                .findByUser_IdAndExam_Id(userId, examId)
                .orElseThrow(() -> new RuntimeException("User chưa join bài thi"));

        participant.setSubmitted(true);
        participant.setSubmitTime(submitTime != null ? submitTime : LocalDateTime.now());

        // Thay thế: Kiểm tra null thủ công (participant ở đây chắc chắn không null, có thể xóa nếu muốn)
        if (participant == null) {
            throw new IllegalArgumentException("participant must not be null");
        }
        return participantRepository.save(participant);
    }

    @Override
    public List<ExamParticipant> getParticipantsByExam(Long examId) {
        // Thêm check null cho param để nhất quán
        if (examId == null) {
            throw new IllegalArgumentException("examId must not be null");
        }
        return participantRepository.findAllByExam_Id(examId);
    }

    @Override
    public ExamParticipant getParticipant(Long userId, Long examId) {
        // Thêm check null cho params để nhất quán
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        if (examId == null) {
            throw new IllegalArgumentException("examId must not be null");
        }
        return participantRepository
                .findByUser_IdAndExam_Id(userId, examId)
                .orElseThrow(() -> new RuntimeException("User chưa join bài thi"));
    }

    @Override
    public Optional<ExamParticipant> findByUserIdAndExamId(Long userId, Long examId) {
        // Thêm check null cho params để nhất quán
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        if (examId == null) {
            throw new IllegalArgumentException("examId must not be null");
        }
        return participantRepository.findByUser_IdAndExam_Id(userId, examId);
    }
}