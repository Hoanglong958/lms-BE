package com.ra.base_spring_boot.dto.analytics;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeacherWorkloadDTO {
    private Long teacherId;
    private String teacherName;
    private long classCount;
    private long activeStudents;
    private long upcomingSessionsNext7Days;
}
