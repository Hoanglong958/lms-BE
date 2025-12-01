package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.dto.ScheduleItem.ScheduleItemResponseDTO;
import com.ra.base_spring_boot.dto.ScheduleItem.GenerateScheduleRequestDTO;
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
import io.swagger.v3.oas.annotations.media.ExampleObject;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
@Tag(name = "24 - Schedules", description = "Quản lý thời khóa biểu")
public class ScheduleItemController {

    private final IScheduleItemService scheduleItemService;

    // ======= Tạo thời khóa biểu (ADMIN) =======
    @PostMapping("/generate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Tạo thời khóa biểu", description = "Chỉ ADMIN được phép tạo thời khóa biểu cho khóa học")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tạo thành công",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ScheduleItemResponseDTO.class)),
                            examples = @ExampleObject(value = "[{\n  \"id\": 1,\n  \"courseId\": 1,\n  \"periodId\": 1,\n  \"sessionNumber\": 1,\n  \"date\": \"2025-12-01\",\n  \"startAt\": \"2025-12-01T08:00:00\",\n  \"endAt\": \"2025-12-01T10:00:00\",\n  \"status\": \"SCHEDULED\"\n}]"))),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền", content = @Content),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống", content = @Content)
    })
    public ResponseEntity<List<ScheduleItemResponseDTO>> generateSchedule(
            @RequestBody @Valid GenerateScheduleRequestDTO request) {
        return ResponseEntity.ok(scheduleItemService.generateScheduleForCourse(request));
    }

    // ======= Lấy thời khóa biểu theo khóa học (ADMIN + USER) =======
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    @Operation(summary = "Lấy thời khóa biểu theo khóa học", description = "Trả về tất cả buổi học của khóa")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ScheduleItemResponseDTO.class)),
                            examples = @ExampleObject(value = "[{\n  \"id\": 1,\n  \"courseId\": 1,\n  \"periodId\": 1,\n  \"sessionNumber\": 1,\n  \"date\": \"2025-12-01\",\n  \"startAt\": \"2025-12-01T08:00:00\",\n  \"endAt\": \"2025-12-01T10:00:00\",\n  \"status\": \"SCHEDULED\"\n}]"))),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền", content = @Content),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống", content = @Content)
    })
    public ResponseEntity<List<ScheduleItemResponseDTO>> getScheduleByCourse(
            @Parameter(description = "Mã khóa học") @PathVariable Long courseId) {
        return ResponseEntity.ok(scheduleItemService.getScheduleByCourse(courseId));
    }

    // ======= Xoá thời khóa biểu của khóa học (ADMIN) =======
    @DeleteMapping("/course/{courseId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Xoá thời khóa biểu của khóa học", description = "Chỉ ADMIN được phép xoá toàn bộ lịch của khóa")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Xoá thành công", content = @Content),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền", content = @Content),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống", content = @Content)
    })
    public ResponseEntity<Void> clearSchedule(
            @Parameter(description = "Mã khóa học") @PathVariable Long courseId) {
        scheduleItemService.clearScheduleForCourse(courseId);
        return ResponseEntity.noContent().build();
    }
}
