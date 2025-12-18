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
public class ClassScheduleResponseDTO {
    private Long classCourseId;
    private Long classId;
    private Long courseId;
    private String className;
    private String courseName;
    private List<ScheduleItemResponseDTO> schedules;
}
