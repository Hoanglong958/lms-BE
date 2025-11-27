package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.Classroom.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IClassService {

    ClassroomResponseDTO create(ClassroomRequestDTO dto);

    ClassroomResponseDTO update(Long id, ClassroomRequestDTO dto);

    void delete(Long id);

    ClassroomResponseDTO findById(Long id);

    List<ClassroomResponseDTO> findAll();

    Page<ClassroomResponseDTO> findAll(Pageable pageable);

    Page<ClassroomResponseDTO> search(String keyword, Pageable pageable);

    ClassStudentResponseDTO enrollStudent(ClassStudentRequestDTO dto);

    void removeStudent(Long classId, Long studentId);

    List<ClassStudentResponseDTO> findStudents(Long classId);

    ClassTeacherResponseDTO assignTeacher(ClassTeacherRequestDTO dto);

    void removeTeacher(Long classId, Long teacherId);

    List<ClassTeacherResponseDTO> findTeachers(Long classId);

    ClassCourseResponseDTO assignCourse(ClassCourseRequestDTO dto);

    void removeCourse(Long classId, Long courseId);

    List<ClassCourseResponseDTO> findCourses(Long classId);

    ClassStatsResponseDTO getClassStats(Long classId);
}
