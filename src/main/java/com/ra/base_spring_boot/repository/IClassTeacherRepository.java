package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.ClassTeacher;
import com.ra.base_spring_boot.model.constants.ClassTeacherRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IClassTeacherRepository extends JpaRepository<ClassTeacher, Long> {

    boolean existsByClazzIdAndTeacherId(Long classId, Long teacherId);

    Optional<ClassTeacher> findByClazzIdAndTeacherId(Long classId, Long teacherId);

    List<ClassTeacher> findByClazzId(Long classId);

    long countByClazzId(Long classId);

    long countByClazzIdAndRole(Long classId, ClassTeacherRole role);
}

