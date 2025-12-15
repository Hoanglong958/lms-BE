package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.Question.QuestionRequestDTO;
import com.ra.base_spring_boot.dto.Question.QuestionResponseDTO;
import com.ra.base_spring_boot.exception.HttpNotFound;
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

    // Lấy tất cả câu hỏi
    @Override
    public List<QuestionResponseDTO> getAll() {
        return questionRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Lấy câu hỏi theo ID
    @Override
    public QuestionResponseDTO getById(Long id) {
        Question question = questionRepository.findById(java.util.Objects.requireNonNull(id, "id must not be null"))
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy câu hỏi với id = " + id));
        return toResponse(question);
    }

    // Tạo câu hỏi mới
    @Override
    public QuestionResponseDTO create(QuestionRequestDTO request) {
        Question question = mapRequestToEntity(request);
        questionRepository.save(java.util.Objects.requireNonNull(question, "question must not be null"));
        return toResponse(question);
    }

    // Tạo nhiều câu hỏi cùng lúc
    @Override
    public List<QuestionResponseDTO> createBulk(List<QuestionRequestDTO> requests) {
        List<Question> entities = java.util.Objects.requireNonNull(requests, "requests must not be null")
                .stream()
                .map(this::mapRequestToEntity)
                .collect(Collectors.toList());

        List<Question> saved = questionRepository.saveAll(java.util.Objects.requireNonNull(entities, "entities must not be null"));
        return saved.stream().map(this::toResponse).collect(Collectors.toList());
    }

    // Cập nhật câu hỏi
    @Override
    public QuestionResponseDTO update(Long id, QuestionRequestDTO request) {
        Question question = questionRepository.findById(java.util.Objects.requireNonNull(id, "id must not be null"))
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy câu hỏi với id = " + id));

        Question updated = mapRequestToEntity(request);
        question.setCategory(updated.getCategory());
        question.setQuestionText(updated.getQuestionText());
        question.setOptions(updated.getOptions());
        question.setCorrectAnswer(updated.getCorrectAnswer());
        question.setExplanation(updated.getExplanation());
        question.setUpdatedAt(LocalDateTime.now());

        questionRepository.save(java.util.Objects.requireNonNull(question, "question must not be null"));
        return toResponse(question);
    }

    // Xóa câu hỏi
    @Override
    public void delete(Long id) {
        if (!questionRepository.existsById(java.util.Objects.requireNonNull(id, "id must not be null"))) {
            throw new RuntimeException("Question not found");
        }
        questionRepository.deleteById(java.util.Objects.requireNonNull(id, "id must not be null"));
    }

    // ===================== Helper =====================

    // Chuyển DTO request sang Entity
    private Question mapRequestToEntity(QuestionRequestDTO request) {
        List<String> optionList = request.getOptions() != null
                ? request.getOptions()
                : List.of();

        return Question.builder()
                .category(request.getCategory())
                .questionText(request.getQuestionText())
                .options(optionList)
                .correctAnswer(request.getCorrectAnswer())
                .explanation(request.getExplanation())
                .createdAt(LocalDateTime.now())
                .build();
    }

    // Chuyển Entity sang DTO response
    private QuestionResponseDTO toResponse(Question question) {
        return QuestionResponseDTO.builder()
                .id(question.getId())
                .category(question.getCategory())
                .questionText(question.getQuestionText())
                .options(question.getOptions())  // List<String>
                .correctAnswer(question.getCorrectAnswer())
                .explanation(question.getExplanation())
                .createdAt(question.getCreatedAt())
                .updatedAt(question.getUpdatedAt())
                .score(null) // điểm chưa gán
                .build();
    }
}
