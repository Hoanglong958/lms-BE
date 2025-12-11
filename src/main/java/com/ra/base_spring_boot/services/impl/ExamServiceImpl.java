package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.Exam.ExamRequestDTO;
import com.ra.base_spring_boot.dto.Exam.ExamResponseDTO;
import com.ra.base_spring_boot.dto.Gmail.EmailDTO;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.*;
import com.ra.base_spring_boot.model.constants.ExamStatus;
import com.ra.base_spring_boot.repository.*;
import com.ra.base_spring_boot.services.IExamService;
import com.ra.base_spring_boot.dto.Question.QuestionResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements IExamService {

    private final IExamRepository examRepository;
    private final ICourseRepository courseRepository;
    private final IQuestionRepository questionRepository;
    private final GmailService gmailService;
    private final IClassStudentRepository classStudentRepository;

    // =====================================================
    // 1) TẠO KỲ THI
    // =====================================================
    @Override
    @Transactional
    public ExamResponseDTO createExam(ExamRequestDTO dto) {

        // ========= VALIDATE =========
        if (dto.getStartTime() == null || dto.getEndTime() == null) {
            throw new HttpBadRequest("Thời gian bắt đầu/kết thúc không được để trống");
        }
        if (!dto.getStartTime().isBefore(dto.getEndTime())) {
            throw new HttpBadRequest("Thời gian bắt đầu phải trước thời gian kết thúc");
        }
        if (dto.getPassingScore() > dto.getMaxScore()) {
            throw new HttpBadRequest("Điểm đạt phải nhỏ hơn hoặc bằng điểm tối đa");
        }

        // ========= KHỞI TẠO EXAM =========
        Exam exam = Exam.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .maxScore(dto.getMaxScore())
                .passingScore(dto.getPassingScore())
                .durationMinutes(dto.getDurationMinutes())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .status(ExamStatus.UPCOMING)
                .createdAt(LocalDateTime.now())
                .examQuestions(new ArrayList<>())
                .totalQuestions(0)
                .build();

        // ========= THÊM CÂU HỎI =========
        if (dto.isAutoAddQuestions()) {
            List<Question> questions = questionRepository.findAll();
            int order = 1;

            for (Question q : questions) {
                exam.getExamQuestions().add(
                        ExamQuestion.builder()
                                .exam(exam)
                                .question(q)
                                .orderIndex(order++)
                                .build()
                );
            }

        } else if (dto.getQuestionIds() != null && !dto.getQuestionIds().isEmpty()) {
            int order = 1;

            for (Long qId : dto.getQuestionIds()) {
                Question q = questionRepository.findById(qId)
                        .orElseThrow(() -> new HttpBadRequest("Question not found: " + qId));

                exam.getExamQuestions().add(
                        ExamQuestion.builder()
                                .exam(exam)
                                .question(q)
                                .orderIndex(order++)
                                .build()
                );
            }
        }

        exam.setTotalQuestions(exam.getExamQuestions().size());

        examRepository.save(exam);

        // ========= GỬI EMAIL THÔNG BÁO =========
        List<ClassStudent> students = classStudentRepository.findByClassroomId(exam.getId());

        for (ClassStudent s : students) {
            User student = s.getStudent();
            gmailService.sendEmail(new EmailDTO(
                    student.getGmail(),
                    "📢 Thông báo bài thi mới",
                    "new_exam",
                    Map.of(
                            "username", student.getFullName(),
                            "examTitle", exam.getTitle(),
                            "startTime", exam.getStartTime(),
                            "endTime", exam.getEndTime()
                    )
            ));
        }

        return mapToResponse(exam);
    }

    // =====================================================
    // 2) CẬP NHẬT KỲ THI
    // =====================================================
    @Override
    @Transactional
    public ExamResponseDTO updateExam(Long examId, ExamRequestDTO dto) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new HttpBadRequest("Exam not found"));

        // ========= VALIDATE =========
        if (dto.getStartTime() == null || dto.getEndTime() == null) {
            throw new HttpBadRequest("Thời gian bắt đầu/kết thúc không được để trống");
        }
        if (!dto.getStartTime().isBefore(dto.getEndTime())) {
            throw new HttpBadRequest("Thời gian bắt đầu phải trước thời gian kết thúc");
        }
        if (dto.getPassingScore() > dto.getMaxScore()) {
            throw new HttpBadRequest("Điểm đạt phải nhỏ hơn hoặc bằng điểm tối đa");
        }

        // ========= UPDATE =========
        exam.setTitle(dto.getTitle());
        exam.setDescription(dto.getDescription());
        exam.setMaxScore(dto.getMaxScore());
        exam.setPassingScore(dto.getPassingScore());
        exam.setDurationMinutes(dto.getDurationMinutes());
        exam.setStartTime(dto.getStartTime());
        exam.setEndTime(dto.getEndTime());
        exam.setUpdatedAt(LocalDateTime.now());

        examRepository.save(exam);
        return mapToResponse(exam);
    }

    // =====================================================
    // 3) XÓA KỲ THI
    // =====================================================
    @Override
    @Transactional
    public void deleteExam(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new HttpBadRequest("Exam not found"));

        exam.getExamQuestions().size();
        if (exam.getExamAttempts() != null) {
            exam.getExamAttempts().size();
        }

        exam.getExamQuestions().clear();
        if (exam.getExamAttempts() != null) {
            exam.getExamAttempts().clear();
        }

        examRepository.delete(exam);
    }

    // =====================================================
    // 4) LẤY CHI TIẾT KỲ THI
    // =====================================================
    @Override
    @Transactional(readOnly = true)
    public ExamResponseDTO getExam(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new HttpBadRequest("Exam not found"));

        return mapToResponse(exam);
    }

    // =====================================================
    // 5) LẤY TẤT CẢ KỲ THI
    // =====================================================
    @Override
    @Transactional(readOnly = true)
    public List<ExamResponseDTO> getAllExams() {
        return examRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // =====================================================
    // 6) THÊM CÂU HỎI VÀO KỲ THI
    // =====================================================
    @Override
    @Transactional
    public void addQuestionsToExam(Long examId, List<Long> questionIds) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new HttpBadRequest("Exam not found"));

        for (Long qId : questionIds) {
            Question q = questionRepository.findById(qId)
                    .orElseThrow(() -> new HttpBadRequest("Question not found: " + qId));

            boolean exists = exam.getExamQuestions().stream()
                    .anyMatch(eq -> eq.getQuestion().getId().equals(qId));
            if (exists) continue;

            exam.getExamQuestions().add(
                    ExamQuestion.builder()
                            .exam(exam)
                            .question(q)
                            .orderIndex(exam.getExamQuestions().size() + 1)
                            .build()
            );
        }

        exam.setTotalQuestions(exam.getExamQuestions().size());
        examRepository.save(exam);
    }

    // =====================================================
    // 7) MAP ENTITY → DTO
    // =====================================================
    private ExamResponseDTO mapToResponse(Exam exam) {

        double pointPerQuestion = exam.getTotalQuestions() > 0
                ? exam.getMaxScore() * 1.0 / exam.getTotalQuestions()
                : 0;

        List<QuestionResponseDTO> questions = exam.getExamQuestions().stream()
                .map(eq -> QuestionResponseDTO.builder()
                        .id(eq.getQuestion().getId())
                        .questionText(eq.getQuestion().getQuestionText())
                        .options(eq.getQuestion().getOptions())
                        .correctAnswer(eq.getQuestion().getCorrectAnswer())
                        .explanation(eq.getQuestion().getExplanation())
                        .score(pointPerQuestion)
                        .build())
                .collect(Collectors.toList());

        return ExamResponseDTO.builder()
                .id(exam.getId())
                .title(exam.getTitle())
                .description(exam.getDescription())
                .totalQuestions(exam.getTotalQuestions())
                .maxScore(exam.getMaxScore())
                .passingScore(exam.getPassingScore())
                .durationMinutes(exam.getDurationMinutes())
                .startTime(exam.getStartTime())
                .endTime(exam.getEndTime())
                .status(exam.getStatus() != null ? exam.getStatus().name() : null)
                .questions(questions)
                .createdAt(exam.getCreatedAt())
                .updatedAt(exam.getUpdatedAt())
                .build();
    }
}
