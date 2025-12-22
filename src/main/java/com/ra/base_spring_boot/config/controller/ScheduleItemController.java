package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.dto.ScheduleItem.*;
import com.ra.base_spring_boot.services.IScheduleItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
@Tag(name = "27 - Schedules", description = "Quản lý thời khóa biểu")
public class ScheduleItemController {

    private final IScheduleItemService scheduleItemService;

    // ===================================================================
    // 1) AUTO GENERATE – Tạo thời khóa biểu tự động
    // ===================================================================
    @PostMapping("/generate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
            summary = "Tạo thời khóa biểu tự động",
            description = "ADMIN tạo thời khóa biểu theo course + class"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tạo thành công",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ScheduleItemResponseDTO.class))
                    )),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ"),
            @ApiResponse(responseCode = "403", description = "Không có quyền")
    })
    public ResponseEntity<List<ScheduleItemResponseDTO>> generateSchedule(
            @RequestBody @Valid GenerateScheduleRequestDTO request
    ) {
        return ResponseEntity.ok(
                scheduleItemService.generateScheduleForCourse(request)
        );
    }

    // ===================================================================
    // 2) MANUAL GENERATE – Tạo thời khóa biểu thủ công
    // ===================================================================
    @PostMapping("/manual")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
            summary = "Tạo thời khóa biểu thủ công",
            description = "ADMIN chọn ngày trong tuần + period"
    )
    public ResponseEntity<List<ScheduleItemResponseDTO>> createManualSchedule(
            @RequestBody @Valid CreateManualScheduleRequestDTO request
    ) {
        return ResponseEntity.ok(
                scheduleItemService.createManualSchedule(request)
        );
    }

    // ===================================================================
    // 3) Lấy toàn bộ lịch của khóa học (ADMIN / REPORT)
    // ===================================================================
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    @Operation(
            summary = "Lấy thời khóa biểu theo course",
            description = "Dùng cho admin / thống kê"
    )
    public ResponseEntity<List<ScheduleItemResponseDTO>> getScheduleByCourse(
            @Parameter(description = "Mã khóa học")
            @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(
                scheduleItemService.getScheduleByCourse(courseId)
        );
    }

    // ===================================================================
    // 4) LẤY LỊCH THEO CLASS_COURSE (API CHUẨN CHO FE)
    // ===================================================================
    @GetMapping("/class-course/{classCourseId}/schedule")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    @Operation(
            summary = "Lấy thời khóa biểu theo class_course",
            description = "API chuẩn: 1 lớp + 1 khóa học"
    )
    public ResponseEntity<ClassScheduleResponseDTO> getScheduleByClassCourse(
            @Parameter(description = "Mã class_course")
            @PathVariable Long classCourseId
    ) {
        return ResponseEntity.ok(
                scheduleItemService.getScheduleByClassCourse(classCourseId)
        );
    }

    // ===================================================================
    // 5) XÓA TOÀN BỘ LỊCH THEO CLASS_COURSE  (ĐÃ SỬA)
    // ===================================================================
    @DeleteMapping("/class-course/{classCourseId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
            summary = "Xóa thời khóa biểu theo class_course",
            description = "Xóa toàn bộ lịch của 1 khóa học trong 1 lớp"
    )
    public ResponseEntity<Void> clearScheduleByClassCourse(
            @PathVariable Long classCourseId
    ) {
        scheduleItemService.clearScheduleForCourse(classCourseId);
        return ResponseEntity.noContent().build();
    }

    // ===================================================================
    // 6) CẬP NHẬT 1 BUỔI HỌC
    // ===================================================================
    @PutMapping("/schedule-items/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
            summary = "Cập nhật buổi học",
            description = "Đổi ngày, period, trạng thái"
    )
    public ResponseEntity<ScheduleItemResponseDTO> updateScheduleItem(
            @PathVariable Long id,
            @RequestBody @Valid UpdateScheduleItemRequestDTO req
    ) {
        return ResponseEntity.ok(
                scheduleItemService.updateScheduleItem(id, req)
        );
    }
}
