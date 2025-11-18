package com.ra.base_spring_boot.config.dto.ExamParticipant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamParticipantRequestDTO {
    private Long userId;
    private Long examId;
    private Long examRoomId;
    private Map<Long, String> answers; // optional, chỉ dùng khi submit
}
