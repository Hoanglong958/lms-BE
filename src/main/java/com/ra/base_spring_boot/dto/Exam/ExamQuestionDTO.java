package com.ra.base_spring_boot.dto.Exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamQuestionDTO {
    private Long id;
    private Long examId;
    private Long questionId;
    private Integer orderIndex;
    private Integer score;
}


