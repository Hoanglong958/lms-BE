package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IQuizResultRepository extends JpaRepository<QuizResult, Long> {

    /**
     * Lấy tất cả kết quả quiz của 1 user (chưa bị xóa mềm)
     */
    List<QuizResult> findByUser_IdAndDeletedFalse(Long userId);

    /**
     * Lấy tất cả kết quả của 1 quiz (chưa bị xóa mềm)
     */
    List<QuizResult> findByQuiz_IdAndDeletedFalse(Long quizId);

    /**
     * Lấy tất cả kết quả chưa bị xóa mềm
     */
    List<QuizResult> findAllByDeletedFalse();

    /**
     * Lấy 1 kết quả cụ thể (chưa bị xóa mềm)
     */
    Optional<QuizResult> findByIdAndDeletedFalse(Long id);
}
