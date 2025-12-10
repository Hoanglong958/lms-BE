package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.Exam.ExamQuestionDTO;
import com.ra.base_spring_boot.exception.HttpNotFound;
import com.ra.base_spring_boot.model.Exam;
import com.ra.base_spring_boot.model.ExamQuestion;
import com.ra.base_spring_boot.model.Question;
import com.ra.base_spring_boot.repository.IExamQuestionRepository;
import com.ra.base_spring_boot.repository.IExamRepository;
import com.ra.base_spring_boot.repository.IQuestionRepository;
import com.ra.base_spring_boot.services.IExamQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamQuestionServiceImpl implements IExamQuestionService {

    private final IExamQuestionRepository examQuestionRepository;
    private final IExamRepository examRepository;
    private final IQuestionRepository questionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ExamQuestionDTO> getByExam(Long examId) {
        Long safeExamId = Objects.requireNonNull(examId, "examId must not be null");
        Exam exam = examRepository.findById(safeExamId)
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy kỳ thi với id = " + examId));
        return exam.getExamQuestions()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ExamQuestionDTO create(ExamQuestionDTO dto) {
        Objects.requireNonNull(dto, "dto must not be null");
        Long dtoExamId = Objects.requireNonNull(dto.getExamId(), "examId must not be null");
        Long dtoQuestionId = Objects.requireNonNull(dto.getQuestionId(), "questionId must not be null");

        Exam exam = examRepository.findById(dtoExamId)
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy kỳ thi với id = " + dtoExamId));
        Question question = questionRepository.findById(dtoQuestionId)
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy câu hỏi với id = " + dtoQuestionId));

        ExamQuestion examQuestion = ExamQuestion.builder()
                .exam(exam)
                .question(question)
                .orderIndex(dto.getOrderIndex())
                .score(dto.getScore() != null ? dto.getScore() : 1)
                .build();

        ExamQuestion saved = examQuestionRepository.save(Objects.requireNonNull(examQuestion, "examQuestion must not be null"));
        return toDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Long safeId = Objects.requireNonNull(id, "id must not be null");
        ExamQuestion examQuestion = examQuestionRepository.findById(safeId)
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy bản ghi exam_question với id = " + id));
        examQuestionRepository.delete(Objects.requireNonNull(examQuestion, "examQuestion must not be null"));
    }

    private ExamQuestionDTO toDto(ExamQuestion entity) {
        return ExamQuestionDTO.builder()
                .id(entity.getId())
                .examId(entity.getExam() != null ? entity.getExam().getId() : null)
                .questionId(entity.getQuestion() != null ? entity.getQuestion().getId() : null)
                .orderIndex(entity.getOrderIndex())
                .score(entity.getScore())
                .build();
    }
}


