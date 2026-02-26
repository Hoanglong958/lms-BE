package com.ra.base_spring_boot.services.course;

import com.ra.base_spring_boot.dto.Course.CourseRequestDTO;
import com.ra.base_spring_boot.dto.Course.CourseResponseDTO;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ICourseService {
    CourseResponseDTO create(CourseRequestDTO dto);
    CourseResponseDTO update(Long id, CourseRequestDTO dto);
    void delete(Long id);
    CourseResponseDTO findById(Long id);
    List<CourseResponseDTO> findAll();
    Page<CourseResponseDTO> findAll(Pageable pageable);
    Page<CourseResponseDTO> search(String keyword, Pageable pageable);
}
