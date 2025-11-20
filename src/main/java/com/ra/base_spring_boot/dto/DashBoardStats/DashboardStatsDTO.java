package com.ra.base_spring_boot.dto.DashBoardStats;

import com.ra.base_spring_boot.dto.Course.CourseResponseDTO;
import com.ra.base_spring_boot.dto.LessonQuizzes.LessonQuizResponseDTO;
import com.ra.base_spring_boot.dto.resp.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
public class DashboardStatsDTO {
    private GrowthItem totalUsers;
    private GrowthItem totalCourses;
    private GrowthItem totalExams;
    private GrowthItem averageExamScore;
    private GrowthItem courseCompletionRate;
    private GrowthItem totalClasses;
    private GrowthItem totalQuizzes;
    private GrowthItem totalAssignments;
    private List<UserResponse> topStudents;
    private List<UserResponse> newUsers;
    private List<CourseResponseDTO> newCourses;
    private List<LessonQuizResponseDTO> recentQuizzes;


    @AllArgsConstructor // đây sẽ tạo public constructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class GrowthItem {
        private long value;
        private double growthPercentage;
    }
}

