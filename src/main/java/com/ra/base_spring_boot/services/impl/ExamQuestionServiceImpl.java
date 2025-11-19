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
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy kỳ thi với id = " + examId));
        return exam.getExamQuestions()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ExamQuestionDTO create(ExamQuestionDTO dto) {
        Exam exam = examRepository.findById(dto.getExamId())
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy kỳ thi với id = " + dto.getExamId()));
        Question question = questionRepository.findById(dto.getQuestionId())
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy câu hỏi với id = " + dto.getQuestionId()));

        ExamQuestion examQuestion = ExamQuestion.builder()
                .exam(exam)
                .question(question)
                .orderIndex(dto.getOrderIndex())
                .score(dto.getScore() != null ? dto.getScore() : 1)
                .build();

        ExamQuestion saved = examQuestionRepository.save(examQuestion);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ExamQuestion examQuestion = examQuestionRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy bản ghi exam_question với id = " + id));
        examQuestionRepository.delete(examQuestion);
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


