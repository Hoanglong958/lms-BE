package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.ClassCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface IClassCourseRepository extends JpaRepository<ClassCourse, Long> {

    boolean existsByClazzIdAndCourseId(Long classId, Long courseId);

    Optional<ClassCourse> findByClazz_IdAndCourse_Id(Long classId, Long courseId);

    List<ClassCourse> findByClazzId(Long classId);

    long countByClazzId(Long classId);

}

