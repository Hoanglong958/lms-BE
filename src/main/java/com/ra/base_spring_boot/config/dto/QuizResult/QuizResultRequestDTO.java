package com.ra.base_spring_boot.config.dto.QuizResult;

import lombok.Data;
import java.util.List;

@Data
public class QuizResultRequestDTO {
    private Long quizId; // ID của bài quiz mà người dùng đang làm
    private Long userId; // nên có nếu bạn cho phép admin submit thay user (nếu không sẽ lấy từ JWT)
    private List<Long> questionIds; // danh sách câu hỏi mà người dùng đã trả lời
    private List<String> selectedAnswers; // câu trả lời người dùng chọn (cùng thứ tự với questionIds)
}
