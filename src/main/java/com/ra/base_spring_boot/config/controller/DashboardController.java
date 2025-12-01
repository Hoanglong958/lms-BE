package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.dto.DashBoardStats.DashboardStatsDTO;
import com.ra.base_spring_boot.dto.DashBoardStats.UserGrowthPointDTO;
import com.ra.base_spring_boot.dto.resp.UserResponse;
import com.ra.base_spring_boot.dto.Course.CourseResponseDTO;
import com.ra.base_spring_boot.dto.LessonQuizzes.LessonQuizResponseDTO;
import com.ra.base_spring_boot.dto.DashBoardStats.QuizReportDTO;
import com.ra.base_spring_boot.services.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "21 - Dashboard", description = "Tổng quan và thống kê hệ thống LMS")
public class DashboardController {

    private final IDashboardService dashboardService;

    // ======= Tổng quan dashboard =======
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @Operation(summary = "Tổng quan dashboard", description = "Hiển thị thống kê tổng quan hệ thống LMS")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DashboardStatsDTO.class))),
            @ApiResponse(responseCode = "403", description = "Không có quyền", content = @Content),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống", content = @Content)
    })
    public ResponseEntity<DashboardStatsDTO> getDashboard() {
        DashboardStatsDTO dto = dashboardService.getDashboard();
        return ResponseEntity.ok(dto);
    }

    // ======= Lượng tăng trưởng người dùng theo tháng =======
    @GetMapping("/user-growth/month")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Lượng tăng trưởng học viên theo tháng", description = "Trả về số lượng học viên theo từng tháng")
    public ResponseEntity<List<UserGrowthPointDTO>> getUserGrowthByMonth(
            @RequestParam(defaultValue = "12") int months) {
        return ResponseEntity.ok(dashboardService.getUserGrowthByMonth(months));
    }

    // ======= Lượng tăng trưởng người dùng theo tuần =======
    @GetMapping("/user-growth/week")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Lượng tăng trưởng học viên theo tuần", description = "Trả về số lượng học viên theo từng tuần")
    public ResponseEntity<List<UserGrowthPointDTO>> getUserGrowthByWeek(
            @RequestParam(defaultValue = "4") int weeks) {
        return ResponseEntity.ok(dashboardService.getUserGrowthByWeek(weeks));
    }



    // ======= Người dùng mới 30 ngày =======
    @GetMapping("/new-users")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    @Operation(summary = "Học viên mới", description = "Danh sách học viên đăng ký trong 30 ngày gần đây")
    public ResponseEntity<List<UserResponse>> getNewUsersLast30Days() {
        return ResponseEntity.ok(dashboardService.getNewUsersLast30Days());
    }

    // ======= Khóa học mới 30 ngày =======
    @GetMapping("/new-courses")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    @Operation(summary = "Khóa học mới", description = "Danh sách khóa học được tạo trong 30 ngày gần đây")
    public ResponseEntity<List<CourseResponseDTO>> getNewCoursesLast30Days() {
        return ResponseEntity.ok(dashboardService.getNewCoursesLast30Days());
    }

    // ======= Quiz mới 30 ngày =======
    @GetMapping("/recent-quizzes")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    @Operation(summary = "Quiz gần đây", description = "Danh sách bài thi/quiz trong 30 ngày gần đây")
    public ResponseEntity<List<LessonQuizResponseDTO>> getRecentQuizzesLast30Days() {
        return ResponseEntity.ok(dashboardService.getRecentQuizzesLast30Days());
    }

    // ======= Báo cáo quiz =======
    @GetMapping("/quiz-reports")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Báo cáo quiz", description = "Kết quả chi tiết các bài thi, điểm TB, tỷ lệ pass/fail")
    public ResponseEntity<List<QuizReportDTO>> getQuizReports() {
        return ResponseEntity.ok(dashboardService.getQuizReports());
    }

    // ======= Tiến độ khóa học =======
    @GetMapping("/course-progress/{courseId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    @Operation(summary = "Tiến độ khóa học", description = "Thông tin hoàn thành/đang học của khóa học theo ID")
    public ResponseEntity<?> getCourseProgress(@Parameter(description = "ID khóa học") @PathVariable Long courseId) {
        return ResponseEntity.ok(dashboardService.getCourseProgress(courseId));
    }
}
