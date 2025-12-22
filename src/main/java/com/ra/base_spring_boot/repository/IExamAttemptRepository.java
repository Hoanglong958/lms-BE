package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.ExamAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IExamAttemptRepository extends JpaRepository<ExamAttempt, Long> {
    List<ExamAttempt> findByExam_Id(Long examId);
    List<ExamAttempt> findByUser_Id(Long userId);
    Optional<ExamAttempt> findTopByExam_IdAndUser_IdOrderByAttemptNumberDesc(Long examId, Long userId);
    Optional<ExamAttempt> findTopByExam_IdAndUser_IdAndStatus(Long examId, Long userId, ExamAttempt.AttemptStatus status);
    @Query("""
    SELECT COUNT(ea)
    FROM ExamAttempt ea
    WHERE ea.endTime IS NOT NULL
""")
    Long countCompletedExams();


    @Query("""
    SELECT COUNT(ea)
    FROM ExamAttempt ea
    WHERE ea.exam.id = :examId
""")
    Long countAttemptByExam(@Param("examId") Long examId);

    @Query("""
    SELECT COUNT(ea)
    FROM ExamAttempt ea
    WHERE ea.exam.id = :examId
      AND ea.score >= ea.exam.passingScore
""")
    Long countPassByExam(@Param("examId") Long examId);


    @Query("""
    SELECT AVG( (ea.score * 10.0) / ea.exam.maxScore )
    FROM ExamAttempt ea
    WHERE ea.score IS NOT NULL
""")
    Double avgExamScoreOnTen();


    @Query("""
    SELECT AVG((ea.score * 10.0) / ea.exam.maxScore)
    FROM ExamAttempt ea
    WHERE ea.score IS NOT NULL
      AND ea.startTime >= :since
""")
    Double avgExamScoreOnTenSince(@Param("since") LocalDateTime since);





}
