package com.ra.base_spring_boot.repository.exam;

import com.ra.base_spring_boot.model.Exam;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface IExamRepository extends JpaRepository<Exam, Long> {

    @Override
    @EntityGraph(attributePaths = "examQuestions")
    @NonNull List<Exam> findAll();

    @Override
    @EntityGraph(attributePaths = "examQuestions")
    @NonNull Optional<Exam> findById(@NonNull Long id);

    @Query("""
    SELECT e
    FROM Exam e
    WHERE e.createdAt >= :since
    ORDER BY e.createdAt DESC
""")
    List<Exam> findRecentExams(@Param("since") LocalDateTime since);

}
