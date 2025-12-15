package com.ra.base_spring_boot.dto.ExamParticipant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamParticipantResponseDTO {
    private Long userId;
    private LocalDateTime startTime;
    private LocalDateTime endTime; // null nếu chưa submit
    private String status; // "JOINED" hoặc "SUBMITTED"
}
