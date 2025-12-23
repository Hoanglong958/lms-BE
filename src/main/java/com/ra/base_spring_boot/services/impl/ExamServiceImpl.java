package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.Exam.ExamRequestDTO;
import com.ra.base_spring_boot.dto.Exam.ExamResponseDTO;
import com.ra.base_spring_boot.model.*;
import com.ra.base_spring_boot.model.constants.ExamStatus;
import com.ra.base_spring_boot.repository.*;
import com.ra.base_spring_boot.services.IExamService;
import com.ra.base_spring_boot.dto.questions.QuestionResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements IExamService {

        private final IExamRepository examRepository;
        private final IQuestionRepository questionRepository;

        // ======= Tạo kỳ thi (ADMIN) =======
        @Override
        @Transactional
        public ExamResponseDTO createExam(ExamRequestDTO dto) {
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

                // ======= Thêm câu hỏi theo yêu cầu =======
                if (dto.isAutoAddQuestions()) {
                        List<Question> questions = questionRepository.findAll();
                        int orderIndex = 1;
                        for (Question q : questions) {
                                ExamQuestion eq = ExamQuestion.builder()
                                                .exam(exam)
                                                .question(q)
                                                .orderIndex(orderIndex++)
                                                .build();
                                exam.getExamQuestions().add(eq);
                        }
                } else if (dto.getQuestionIds() != null && !dto.getQuestionIds().isEmpty()) {
                        int orderIndex = 1;
                        for (Long qId : dto.getQuestionIds()) {
                                Question q = questionRepository.findById(qId)
                                                .orElseThrow(() -> new RuntimeException("Question not found: " + qId));
                                ExamQuestion eq = ExamQuestion.builder()
                                                .exam(exam)
                                                .question(q)
                                                .orderIndex(orderIndex++)
                                                .build();
                                exam.getExamQuestions().add(eq);
                        }
                }

                exam.setTotalQuestions(exam.getExamQuestions().size());

                examRepository.save(exam);

                return mapToResponse(exam);
        }

        // ======= Cập nhật kỳ thi (ADMIN) =======
        @Override
        @Transactional
        public ExamResponseDTO updateExam(Long examId, ExamRequestDTO dto) {
                Exam exam = examRepository.findById(examId)
                                .orElseThrow(() -> new RuntimeException("Exam not found"));

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

        // ======= Xóa kỳ thi (ADMIN) =======
        @Override
        @Transactional
        public void deleteExam(Long examId) {
                Exam exam = examRepository.findById(examId)
                                .orElseThrow(() -> new RuntimeException("Exam not found"));

                // Force load lazy collections trước khi xóa
                exam.getExamQuestions().size();
                if (exam.getExamAttempts() != null) {
                        exam.getExamAttempts().size();
                }

                // Xóa tất cả child
                exam.getExamQuestions().clear();
                if (exam.getExamAttempts() != null) {
                        exam.getExamAttempts().clear();
                }

                // Xóa parent
                examRepository.delete(exam);
        }

        // ======= Lấy kỳ thi theo ID =======
        @Override
        @Transactional(readOnly = true)
        public ExamResponseDTO getExam(Long examId) {
                Exam exam = examRepository.findById(examId)
                                .orElseThrow(() -> new RuntimeException("Exam not found"));
                return mapToResponse(exam);
        }

        // ======= Lấy danh sách tất cả kỳ thi =======
        @Override
        @Transactional(readOnly = true)
        public List<ExamResponseDTO> getAllExams() {
                return examRepository.findAll()
                                .stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        // ======= Thêm câu hỏi hiện có vào kỳ thi =======
        @Override
        @Transactional
        public void addQuestionsToExam(Long examId, List<Long> questionIds) {
                Exam exam = examRepository.findById(examId)
                                .orElseThrow(() -> new RuntimeException("Exam not found"));

                for (Long questionId : questionIds) {
                        Question question = questionRepository.findById(questionId)
                                        .orElseThrow(() -> new RuntimeException("Question not found: " + questionId));

                        boolean exists = exam.getExamQuestions().stream()
                                        .anyMatch(eq -> eq.getQuestion().getId().equals(questionId));
                        if (exists)
                                continue;

                        ExamQuestion eq = ExamQuestion.builder()
                                        .exam(exam)
                                        .question(question)
                                        .orderIndex(exam.getExamQuestions().size() + 1)
                                        .build();

                        exam.getExamQuestions().add(eq);
                }

                exam.setTotalQuestions(exam.getExamQuestions().size());
                examRepository.save(exam);
        }

        // ======= Chuyển Entity -> DTO (bao gồm danh sách câu hỏi) =======
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
