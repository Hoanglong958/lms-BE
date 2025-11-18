package com.ra.base_spring_boot.config.dto.Exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class AddQuestionsToExamDTO {
    private List<Long> questionIds; // Danh sách ID câu hỏi cần thêm
}
