package com.ra.base_spring_boot.config.dto.Statistic;

import com.ra.base_spring_boot.config.dto.resp.UserResponse;
import com.ra.base_spring_boot.config.dto.Course.CourseResponseDTO;
import com.ra.base_spring_boot.config.dto.LessonQuizzes.LessonQuizResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {

    private GrowthItem totalUsers;          // tổng user + % tăng
    private GrowthItem totalCourses;        // tổng courses
    private GrowthItem totalExams;          // tổng bài thi
    private GrowthItem averageExamScore;    // điểm TB
    private GrowthItem courseCompletionRate;// % hoàn thành
    private GrowthItem totalClasses;        // tổng lớp
    private GrowthItem totalQuizzes;        // tổng quiz
    private GrowthItem totalAssignments;    // tổng bài tập

    private List<UserResponse> topUsers;               // top 10 học viên
    private List<CourseResponseDTO> newCourses;          // khóa học mới
    private List<LessonQuizResponseDTO> recentQuizzes;   // quiz gần đây
    private List<UserResponse> newUsers;                 // user mới

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static
    class GrowthItem {
        private long total;
        private double growthPercent; // % so với tháng trước
    }
}

