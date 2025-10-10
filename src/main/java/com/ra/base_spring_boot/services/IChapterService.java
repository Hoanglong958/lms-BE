package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.Chapter.ChapterRequestDTO;
import com.ra.base_spring_boot.dto.Chapter.ChapterResponseDTO;
import java.util.List;

public interface IChapterService {
    List<ChapterResponseDTO> getByCourse(Long courseId);
    ChapterResponseDTO getById(Long id);
    ChapterResponseDTO create(ChapterRequestDTO dto);
    void delete(Long id);
}
