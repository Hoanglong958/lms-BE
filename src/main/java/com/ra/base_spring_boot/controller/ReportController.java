package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.services.common.IReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "24 - Reports", description = "Xuất báo cáo PDF và Excel cho Dashboard")
public class ReportController {

    private final IReportService reportService;

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Xuất báo cáo", description = "Xuất các loại báo cáo (users, courses, progress, quizzes, revenue) dưới dạng Excel hoặc PDF")
    public ResponseEntity<byte[]> exportReport(
            @RequestParam String report, // users, courses, progress, quizzes, revenue
            @RequestParam String type // excel, pdf
    ) {
        byte[] data;
        String fileName;
        MediaType mediaType;

        switch (report.toLowerCase()) {
            case "users":
                data = reportService.exportUsers(type);
                fileName = "user_report";
                break;
            case "courses":
                data = reportService.exportCourses(type);
                fileName = "course_report";
                break;
            case "progress":
                data = reportService.exportStudentProgress(type);
                fileName = "student_progress_report";
                break;
            case "quizzes":
                data = reportService.exportQuizReports(type);
                fileName = "quiz_report";
                break;
            case "revenue":
                data = reportService.exportRevenue(type);
                fileName = "revenue_report";
                break;
            default:
                return ResponseEntity.badRequest().build();
        }

        if (type.equalsIgnoreCase("excel")) {
            fileName += ".xlsx";
            mediaType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        } else {
            fileName += ".pdf";
            mediaType = MediaType.APPLICATION_PDF;
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(mediaType)
                .body(data);
    }
}
