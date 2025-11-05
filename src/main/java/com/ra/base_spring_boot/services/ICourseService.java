package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.Course.CourseRequestDTO;
import com.ra.base_spring_boot.dto.Course.CourseResponseDTO;

import java.util.List;

public interface ICourseService {
    CourseResponseDTO create(CourseRequestDTO dto);
    CourseResponseDTO update(Long id, CourseRequestDTO dto);
    void delete(Long id);
    CourseResponseDTO findById(Long id);
    List<CourseResponseDTO> findAll();
}
