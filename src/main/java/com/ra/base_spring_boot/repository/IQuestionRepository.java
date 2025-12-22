package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IQuestionRepository extends JpaRepository<Question, Long> {

    Page<Question> findByQuestionTextContainingIgnoreCase(
            String keyword,
            Pageable pageable
    );

    Page<Question> findByCategoryIgnoreCase(
            String category,
            Pageable pageable
    );

    Page<Question> findByQuestionTextContainingIgnoreCaseAndCategoryIgnoreCase(
            String keyword,
            String category,
            Pageable pageable
    );
}


