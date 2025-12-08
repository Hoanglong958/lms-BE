package com.ra.base_spring_boot.dto.ScheduleItem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class GenerateScheduleRequestDTO {
    private Long courseId;
    private int sessionsPerWeek;
    private Long classId;

}