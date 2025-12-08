package com.ra.base_spring_boot.dto.ScheduleItem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateManualScheduleRequestDTO {
    private Long courseId;
    private List<Integer> daysOfWeek;   // Ví dụ [2,4]
    private List<Long> periodIds;       // Ví dụ [1,2]
    private Long classId;
}