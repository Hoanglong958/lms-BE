package com.ra.base_spring_boot.dto.analytics;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentSuccessAnalyticsResponse {
    private Long classId;
    private String className;
    private double averageAttendanceRate;
    private double averageProgressPercent;
    private double averageScorePercent;
    private double attendanceProgressCorrelation;
    private double attendanceScoreCorrelation;
    private double progressScoreCorrelation;
    private List<StudentRiskDTO> atRiskStudents;
    private List<MasteryMetricDTO> masteryByLessonType;
    private List<SkillMasteryDTO> masteryBySkill;
    private List<TeacherWorkloadDTO> teacherWorkloads;
}
