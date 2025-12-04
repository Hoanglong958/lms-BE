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
import io.swagger.v3.oas.annotations.media.ExampleObject;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
@Tag(name = "24 - Schedules", description = "Quản lý thời khóa biểu")
public class ScheduleItemController {

    private final IScheduleItemService scheduleItemService;

    // ===================================================================
    // 1) AUTO GENERATE – Tạo thời khóa biểu tự động
    // ===================================================================
    @PostMapping("/generate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Tạo thời khóa biểu tự động", description = "Chỉ ADMIN được phép tạo thời khóa biểu tự động theo cấu hình daysOfWeek + periodIds")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tạo thành công",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ScheduleItemResponseDTO.class))
                    )),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền"),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
    })
    public ResponseEntity<List<ScheduleItemResponseDTO>> generateSchedule(
            @RequestBody @Valid GenerateScheduleRequestDTO request
    ) {
        return ResponseEntity.ok(scheduleItemService.generateScheduleForCourse(request));
    }

    // ===================================================================
    // 2) MANUAL GENERATE – Tạo thời khóa biểu thủ công
    // ===================================================================
    @PostMapping("/manual")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Tạo thời khóa biểu thủ công", description = "ADMIN chọn trực tiếp các ngày trong tuần và period tương ứng")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tạo thành công",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ScheduleItemResponseDTO.class)),
                            examples = @ExampleObject(value =
                                    "{ \"courseId\": 1, \"daysOfWeek\": [2,4], \"periodIds\": [1,3] }"
                            )
                    )),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền"),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
    })
    public ResponseEntity<List<ScheduleItemResponseDTO>> createManualSchedule(
            @RequestBody @Valid CreateManualScheduleRequestDTO request
    ) {
        return ResponseEntity.ok(scheduleItemService.createManualSchedule(request));
    }

    // ===================================================================
    // 3) Lấy toàn bộ lịch của khóa học
    // ===================================================================
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    @Operation(summary = "Lấy thời khóa biểu theo khóa học", description = "Trả về tất cả các buổi học của một khóa học")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ScheduleItemResponseDTO.class))
                    )),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền"),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
    })
    public ResponseEntity<List<ScheduleItemResponseDTO>> getScheduleByCourse(
            @Parameter(description = "Mã khóa học") @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(scheduleItemService.getScheduleByCourse(courseId));
    }

    // ===================================================================
    // 4) Xóa toàn bộ lịch của khóa học
    // ===================================================================
    @DeleteMapping("/course/{courseId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Xóa thời khóa biểu của khóa học", description = "Chỉ ADMIN được phép xóa toàn bộ lịch của khóa")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Xóa thành công"),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền"),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
    })
    public ResponseEntity<Void> clearSchedule(
            @Parameter(description = "Mã khóa học") @PathVariable Long courseId
    ) {
        scheduleItemService.clearScheduleForCourse(courseId);
        return ResponseEntity.noContent().build();
    }

    // ===================================================================
    // 5) Cập nhật 1 buổi học
    // ===================================================================
    @PutMapping("/schedule-items/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Cập nhật thông tin buổi học", description = "Dùng để đổi ngày, đổi period, đổi trạng thái…")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = @Content(schema = @Schema(implementation = ScheduleItemResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền"),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
    })
    public ResponseEntity<ScheduleItemResponseDTO> updateScheduleItem(
            @PathVariable Long id,
            @RequestBody @Valid UpdateScheduleItemRequestDTO req
    ) {
        return ResponseEntity.ok(scheduleItemService.updateScheduleItem(id, req));
    }

}
