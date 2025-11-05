package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.Exam.ExamRequestDTO;
import com.ra.base_spring_boot.dto.Exam.ExamResponseDTO;
import com.ra.base_spring_boot.model.*;
import com.ra.base_spring_boot.model.constants.ExamStatus;
import com.ra.base_spring_boot.repository.*;
import com.ra.base_spring_boot.services.IExamService;
import com.ra.base_spring_boot.config.controller.ExamSocketController; // ✅ đúng import
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements IExamService {

    private final IExamRepository examRepository;
    private final ICourseRepository courseRepository;
    private final IQuestionRepository questionRepository;
    private final IExamQuestionRepository examQuestionRepository;
    private final ModelMapper modelMapper;
    private final ExamSocketController examSocketController; // ✅ sửa lại đúng

    // ======= Tạo kỳ thi (ADMIN) =======
    @Override
    public ExamResponseDTO createExam(ExamRequestDTO dto) {
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        Exam exam = Exam.builder()
                .course(course)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .maxScore(dto.getMaxScore())
                .passingScore(dto.getPassingScore())
                .durationMinutes(dto.getDurationMinutes())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .status(ExamStatus.UPCOMING)
                .createdAt(LocalDateTime.now())
                .totalQuestions(0)
                .build();

        examRepository.save(exam);

        // ✅ Sau khi tạo xong kỳ thi -> gửi thông báo qua WebSocket
        ExamResponseDTO response = mapToResponse(exam);
        examSocketController.sendExamCreatedNotification(response);

        return response;
    }

    // ======= Cập nhật kỳ thi (ADMIN) =======
    @Override
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
    public void deleteExam(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));
        examRepository.delete(exam);
    }

    // ======= Lấy kỳ thi theo ID =======
    @Override
    public ExamResponseDTO getExam(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));
        return mapToResponse(exam);
    }

    // ======= Lấy danh sách tất cả kỳ thi =======
    @Override
    public List<ExamResponseDTO> getAllExams() {
        return examRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ======= Thêm câu hỏi vào kỳ thi =======
    @Override
    public void addQuestionsToExam(Long examId, List<Long> questionIds) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        for (Long questionId : questionIds) {
            Question question = questionRepository.findById(questionId)
                    .orElseThrow(() -> new RuntimeException("Question not found: " + questionId));

            boolean exists = exam.getExamQuestions().stream()
                    .anyMatch(eq -> eq.getQuestion().getId().equals(questionId));
            if (exists) continue;

            ExamQuestion eq = ExamQuestion.builder()
                    .exam(exam)
                    .question(question)
                    .orderIndex(exam.getExamQuestions().size() + 1)
                    .score(1)
                    .build();

            examQuestionRepository.save(eq);
        }

        exam.setTotalQuestions(exam.getExamQuestions().size());
        examRepository.save(exam);
    }

    // ======= Hàm chuyển Entity -> DTO =======
    private ExamResponseDTO mapToResponse(Exam exam) {
        ExamResponseDTO dto = modelMapper.map(exam, ExamResponseDTO.class);
        dto.setStatus(exam.getStatus().name());
        return dto;
    }
}
