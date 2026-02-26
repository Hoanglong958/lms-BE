package com.ra.base_spring_boot.repository.classroom;

import com.ra.base_spring_boot.model.ClassTeacher;
import com.ra.base_spring_boot.model.constants.ClassTeacherRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

public interface IClassTeacherRepository extends JpaRepository<ClassTeacher, Long> {

    boolean existsByClazzIdAndTeacherId(Long classId, Long teacherId);

    Optional<ClassTeacher> findByClazzIdAndTeacherId(Long classId, Long teacherId);

    @EntityGraph(attributePaths = { "clazz", "teacher" })
    List<ClassTeacher> findByClazzId(Long classId);

    long countByClazzId(Long classId);

    long countByClazzIdAndRole(Long classId, ClassTeacherRole role);

    @EntityGraph(attributePaths = { "clazz", "teacher" })
    List<ClassTeacher> findByTeacherId(Long teacherId);
}
