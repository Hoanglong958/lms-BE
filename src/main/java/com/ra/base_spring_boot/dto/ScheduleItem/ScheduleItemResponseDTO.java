package com.ra.base_spring_boot.dto.ScheduleItem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ScheduleItemResponseDTO {
    private Long id;
    private Long classId;
    private Long courseId;
    private Long periodId;
    private int sessionNumber;
    private LocalDate date;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String status;
}
