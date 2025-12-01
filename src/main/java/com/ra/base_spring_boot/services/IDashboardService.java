package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.Course.CourseResponseDTO;
import com.ra.base_spring_boot.dto.DashBoardStats.CourseProgressDTO;
import com.ra.base_spring_boot.dto.DashBoardStats.DashboardStatsDTO;
import com.ra.base_spring_boot.dto.DashBoardStats.QuizReportDTO;
import com.ra.base_spring_boot.dto.DashBoardStats.UserGrowthPointDTO;
import com.ra.base_spring_boot.dto.LessonQuizzes.LessonQuizResponseDTO;
import com.ra.base_spring_boot.dto.resp.UserResponse;

import java.util.List;

public interface IDashboardService {
    DashboardStatsDTO getDashboard();
    List<UserGrowthPointDTO> getUserGrowthByMonth(int months); // months back
    List<UserGrowthPointDTO> getUserGrowthByWeek(int weeks);
    CourseProgressDTO getCourseProgress(Long courseId);
    List<UserResponse> getNewUsersLast30Days();
    List<CourseResponseDTO> getNewCoursesLast30Days();
    List<LessonQuizResponseDTO> getRecentQuizzesLast30Days();
    List<QuizReportDTO> getQuizReports(); // general quiz reports
}
