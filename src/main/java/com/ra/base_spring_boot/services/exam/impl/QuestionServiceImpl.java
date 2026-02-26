package com.ra.base_spring_boot.services.exam.impl;

import com.ra.base_spring_boot.dto.questions.QuestionRequestDTO;
import com.ra.base_spring_boot.dto.questions.QuestionResponseDTO;
import com.ra.base_spring_boot.exception.HttpNotFound;
import com.ra.base_spring_boot.model.Question;
import com.ra.base_spring_boot.repository.exam.IQuestionRepository;
import com.ra.base_spring_boot.services.exam.IQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionServiceImpl implements IQuestionService {

        private final IQuestionRepository questionRepository;

        // ====== CONSTANTS ======
        private static final int DEFAULT_PAGE = 0;
        private static final int DEFAULT_SIZE = 10;
        private static final int MAX_SIZE = 50;

        // ================== GET QUESTIONS (PAGINATION) ==================
        @Override
        public Page<QuestionResponseDTO> getQuestions(
                        Integer page,
                        Integer size,
                        String keyword,
                        String category) {

                // ===== PAGE =====
                int safePage = (page == null || page < 0)
                                ? DEFAULT_PAGE
                                : page;

                // ===== SIZE =====
                int safeSize;
                if (size == null || size <= 0) {
                        safeSize = DEFAULT_SIZE;
                } else if (size > MAX_SIZE) {
                        safeSize = MAX_SIZE;
                } else {
                        safeSize = size;
                }

                Pageable pageable = PageRequest.of(
                                safePage,
                                safeSize,
                                Sort.by("createdAt").descending());

                boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
                boolean hasCategory = category != null && !category.trim().isEmpty();

                Page<Question> pageData;

                if (hasKeyword && hasCategory) {
                        pageData = questionRepository
                                        .findByQuestionTextContainingIgnoreCaseAndCategoryIgnoreCase(
                                                        keyword.trim(),
                                                        category.trim(),
                                                        pageable);
                } else if (hasKeyword) {
                        pageData = questionRepository
                                        .findByQuestionTextContainingIgnoreCase(
                                                        keyword.trim(),
                                                        pageable);
                } else if (hasCategory) {
                        pageData = questionRepository
                                        .findByCategoryIgnoreCase(
                                                        category.trim(),
                                                        pageable);
                } else {
                        pageData = questionRepository.findAll(pageable);
                }

                return pageData.map(this::toResponse);
        }

        // ================== GET BY ID ==================
        @Override
        public QuestionResponseDTO getById(Long id) {
                Question question = questionRepository.findById(id)
                                .orElseThrow(() -> new HttpNotFound("Không tìm thấy câu hỏi với id = " + id));
                return toResponse(question);
        }

        // ================== CREATE ==================
        @Override
        public QuestionResponseDTO create(QuestionRequestDTO request) {
                Question question = mapRequestToEntity(request);
                questionRepository.save(question);
                return toResponse(question);
        }

        // ================== CREATE BULK ==================
        @Override
        public List<QuestionResponseDTO> createBulk(List<QuestionRequestDTO> requests) {
                List<Question> entities = new ArrayList<>();
                for (QuestionRequestDTO request : requests) {
                        entities.add(mapRequestToEntity(request));
                }

                List<Question> savedEntities = questionRepository.saveAll(entities);
                return savedEntities.stream()
                                .map(this::toResponse)
                                .collect(Collectors.toList());
        }

        // ================== UPDATE ==================
        @Override
        public QuestionResponseDTO update(Long id, QuestionRequestDTO request) {
                Question question = questionRepository.findById(id)
                                .orElseThrow(() -> new HttpNotFound("Không tìm thấy câu hỏi với id = " + id));

                question.setCategory(request.getCategory());
                question.setQuestionText(request.getQuestionText());
                question.setOptions(request.getOptions());
                question.setCorrectAnswer(request.getCorrectAnswer());
                question.setExplanation(request.getExplanation());
                question.setUpdatedAt(LocalDateTime.now());

                questionRepository.save(question);
                return toResponse(question);
        }

        // ================== DELETE ==================
        @Override
        public void delete(Long id) {
                if (!questionRepository.existsById(id)) {
                        throw new HttpNotFound("Không tìm thấy câu hỏi với id = " + id);
                }
                questionRepository.deleteById(id);
        }

        @Override
        public Page<com.ra.base_spring_boot.dto.questions.CategoryResponseDTO> getCategories(Integer page,
                        Integer size) {
                int safePage = (page == null || page < 0) ? DEFAULT_PAGE : page;
                int safeSize = (size == null || size <= 0) ? DEFAULT_SIZE : (size > MAX_SIZE ? MAX_SIZE : size);
                Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by("category").ascending());
                return questionRepository.findUniqueCategories(pageable);
        }

        // ===================== HELPER =====================

        private Question mapRequestToEntity(QuestionRequestDTO request) {
                return Question.builder()
                                .category(request.getCategory())
                                .questionText(request.getQuestionText())
                                .options(
                                                request.getOptions() != null
                                                                ? request.getOptions()
                                                                : List.of())
                                .correctAnswer(request.getCorrectAnswer())
                                .explanation(request.getExplanation())
                                .createdAt(LocalDateTime.now())
                                .build();
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
                                .score(null)
                                .build();
        }
}
