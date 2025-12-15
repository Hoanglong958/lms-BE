package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.Question.QuestionRequestDTO;
import com.ra.base_spring_boot.dto.Question.QuestionResponseDTO;
import java.util.List;

public interface IQuestionService {
    List<QuestionResponseDTO> getAll();
    QuestionResponseDTO getById(Long id);
    QuestionResponseDTO create(QuestionRequestDTO request);
    List<QuestionResponseDTO> createBulk(List<QuestionRequestDTO> requests);
    QuestionResponseDTO update(Long id, QuestionRequestDTO request);
    void delete(Long id);
}
