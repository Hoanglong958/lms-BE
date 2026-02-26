package com.ra.base_spring_boot.repository.exam;

import com.ra.base_spring_boot.model.ExamQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IExamQuestionRepository extends JpaRepository<ExamQuestion, Long> {
}
