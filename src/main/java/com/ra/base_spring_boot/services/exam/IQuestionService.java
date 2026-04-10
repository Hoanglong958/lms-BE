package com.ra.base_spring_boot.services.exam;

import com.ra.base_spring_boot.dto.questions.QuestionRequestDTO;
import com.ra.base_spring_boot.dto.questions.QuestionResponseDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IQuestionService {
    Page<QuestionResponseDTO> getQuestions(
            Integer page,
            Integer size,
            String keyword,
            String category);

    QuestionResponseDTO getById(Long id);

    QuestionResponseDTO create(QuestionRequestDTO request);

    List<QuestionResponseDTO> createBulk(List<QuestionRequestDTO> requests);

    QuestionResponseDTO update(Long id, QuestionRequestDTO request);

    void delete(Long id);

    Page<com.ra.base_spring_boot.dto.questions.CategoryResponseDTO> getCategories(Integer page, Integer size);
}
