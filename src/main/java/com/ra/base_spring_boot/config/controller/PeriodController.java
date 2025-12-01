package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.dto.Period.PeriodRequestDTO;
import com.ra.base_spring_boot.dto.Period.PeriodResponseDTO;
import com.ra.base_spring_boot.services.IPeriodService;
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
@RequestMapping("/api/v1/periods")
@RequiredArgsConstructor
@Tag(name = "23 - Periods", description = "Quản lý ca học")
public class PeriodController {

    private final IPeriodService periodService;

    // ======= Tạo ca học (ADMIN) =======
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Tạo ca học", description = "Chỉ ADMIN được phép tạo ca học")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tạo thành công",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PeriodResponseDTO.class),
                            examples = @ExampleObject(value = "{\n  \"id\": 1,\n  \"name\": \"Ca sáng\",\n  \"startTime\": \"08:00:00\",\n  \"endTime\": \"10:00:00\",\n  \"createdAt\": \"2025-11-28T09:30:00\",\n  \"updatedAt\": \"2025-11-28T09:30:00\"\n}"))),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền", content = @Content),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống", content = @Content)
    })
    public ResponseEntity<PeriodResponseDTO> createPeriod(@RequestBody @Valid PeriodRequestDTO request) {
        return ResponseEntity.ok(periodService.create(request));
    }

    // ======= Cập nhật ca học (ADMIN) =======
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Cập nhật ca học", description = "Chỉ ADMIN được phép cập nhật ca học")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PeriodResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền", content = @Content),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống", content = @Content)
    })
    public ResponseEntity<PeriodResponseDTO> updatePeriod(
            @Parameter(description = "Mã ca học") @PathVariable Long id,
            @RequestBody @Valid PeriodRequestDTO request) {
        return ResponseEntity.ok(periodService.update(id, request));
    }

    // ======= Xóa ca học (ADMIN) =======
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Xóa ca học", description = "Chỉ ADMIN được phép xóa ca học")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Xóa thành công", content = @Content),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền", content = @Content),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống", content = @Content)
    })
    public ResponseEntity<Void> deletePeriod(@Parameter(description = "Mã ca học") @PathVariable Long id) {
        periodService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ======= Lấy danh sách tất cả ca học (ADMIN + USER) =======
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @Operation(summary = "Danh sách ca học", description = "Trả về tất cả ca học")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PeriodResponseDTO.class)),
                            examples = @ExampleObject(value = "[{\n  \"id\": 1,\n  \"name\": \"Ca sáng\",\n  \"startTime\": \"08:00:00\",\n  \"endTime\": \"10:00:00\",\n  \"createdAt\": \"2025-11-28T09:30:00\",\n  \"updatedAt\": \"2025-11-28T09:30:00\"\n}]"))),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền", content = @Content),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống", content = @Content)
    })
    public ResponseEntity<List<PeriodResponseDTO>> getAllPeriods() {
        return ResponseEntity.ok(periodService.findAll());
    }
}
