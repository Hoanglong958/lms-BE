package com.ra.base_spring_boot.services.quiz;

import com.ra.base_spring_boot.dto.QuizResult.QuizResultResponseDTO;
import com.ra.base_spring_boot.dto.QuizResult.QuizSubmissionRequestDTO;
import com.ra.base_spring_boot.model.QuizResult;

import java.util.List;

/**
 * IQuizResultService - Interface định nghĩa nghiệp vụ
 * liên quan đến kết quả làm bài Quiz của học viên.
 */
public interface IQuizResultService {

    /**
     * Lấy danh sách tất cả kết quả quiz (dành cho admin).
     * Chỉ trả về kết quả chưa bị xóa mềm.
     */
    List<QuizResultResponseDTO> findAll();

    /**
     * Tìm kết quả quiz theo ID.
     * Chỉ trả về nếu chưa bị xóa mềm.
     */
    QuizResultResponseDTO findById(Long id);

    /**
     * Lưu mới hoặc cập nhật kết quả quiz.
     */
    QuizResult save(QuizResult quizResult);

    /**
     * Xóa mềm kết quả quiz (đánh dấu deleted = true).
     * Không xóa vật lý trong cơ sở dữ liệu.
     */
    void delete(Long id);

    /**
     * Khôi phục kết quả quiz đã bị xóa mềm (nếu cần).
     */
    void restore(Long id);

    /**
     * Xử lý logic khi người dùng nộp bài quiz.
     */
    QuizResultResponseDTO submitQuiz(QuizSubmissionRequestDTO request);
}
