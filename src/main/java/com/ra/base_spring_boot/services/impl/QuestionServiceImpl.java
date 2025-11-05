package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.Question.QuestionRequestDTO;
import com.ra.base_spring_boot.dto.Question.QuestionResponseDTO;
import com.ra.base_spring_boot.model.Question;
import com.ra.base_spring_boot.repository.IQuestionRepository;
import com.ra.base_spring_boot.services.IQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionServiceImpl implements IQuestionService {

    private final IQuestionRepository questionRepository;

    @Override
    public List<QuestionResponseDTO> getAll() {
        return questionRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public QuestionResponseDTO getById(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        return toResponse(question);
    }

    @Override
    public QuestionResponseDTO create(QuestionRequestDTO request) {
        Question question = Question.builder()
                .category(request.getCategory())
                .questionText(request.getQuestionText())
                .options(request.getOptions())
                .correctAnswer(request.getCorrectAnswer())
                .explanation(request.getExplanation())
                .createdAt(LocalDateTime.now())
                .build();
        questionRepository.save(question);
        return toResponse(question);
    }

    @Override
    public QuestionResponseDTO update(Long id, QuestionRequestDTO request) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        question.setCategory(request.getCategory());
        question.setQuestionText(request.getQuestionText());
        question.setOptions(request.getOptions());
        question.setCorrectAnswer(request.getCorrectAnswer());
        question.setExplanation(request.getExplanation());
        question.setUpdatedAt(LocalDateTime.now());

        questionRepository.save(question);
        return toResponse(question);
    }

    @Override
    public void delete(Long id) {
        if (!questionRepository.existsById(id)) {
            throw new RuntimeException("Question not found");
        }
        questionRepository.deleteById(id);
    }

    private QuestionResponseDTO toResponse(Question question) {
        return QuestionResponseDTO.builder()
                .id(question.getId())
                .category(question.getCategory())
                .questionText(question.getQuestionText())
                .options(question.getOptions())
                .correctAnswer(question.getCorrectAnswer())
                .explanation(question.getExplanation())
                .createdAt(question.getCreatedAt())
                .updatedAt(question.getUpdatedAt())
                .build();
    }
}
