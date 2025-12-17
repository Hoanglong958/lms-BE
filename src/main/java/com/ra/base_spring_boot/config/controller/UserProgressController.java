package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.dto.UserProgress.*;
import com.ra.base_spring_boot.services.IUserProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user-progress")
@RequiredArgsConstructor
@Tag(name = "19 - User Progress", description = "Tiến độ khóa học, session và lesson của người dùng")
public class UserProgressController {

    private final IUserProgressService userProgressService;

    // ===== Khóa học =====
    @PostMapping("/courses")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    @Operation(
            summary = "Cập nhật/ghi danh tiến độ khóa học",
            description = "Nếu chưa có bản ghi sẽ tự tạo mới, nếu đã tồn tại sẽ cập nhật tiến độ hiện tại"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tiến độ khóa học sau khi cập nhật",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserCourseProgressResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "CourseProgressExample",
                                    value = "{\n" +
                                            "  \"id\": 1,\n" +
                                            "  \"userId\": 5,\n" +
                                            "  \"userName\": \"Nguyễn Văn A\",\n" +
                                            "  \"courseId\": 3,\n" +
                                            "  \"courseTitle\": \"Spring Boot Fundamentals\",\n" +
                                            "  \"enrolledAt\": \"2025-01-05T08:00:00\",\n" +
                                            "  \"progressPercent\": 25.50,\n" +
                                            "  \"completedSessions\": 3,\n" +
                                            "  \"totalSessions\": 10,\n" +
                                            "  \"status\": \"IN_PROGRESS\",\n" +
                                            "  \"lastAccessedAt\": \"2025-01-06T09:00:00\"\n" +
                                            "}"
                            ))),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền", content = @Content),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống", content = @Content)
    })
    public ResponseEntity<UserCourseProgressResponseDTO> upsertCourseProgress(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Thông tin tiến độ khóa học cần cập nhật",
                    content = @Content(schema = @Schema(implementation = UserCourseProgressRequestDTO.class),
                            examples = @ExampleObject(
                                    name = "CourseProgressRequest",
                                    value = "{\n" +
                                            "  \"userId\": 5,\n" +
                                            "  \"courseId\": 3,\n" +
                                            "  \"progressPercent\": 25.50,\n" +
                                            "  \"completedSessions\": 3,\n" +
                                            "  \"totalSessions\": 10,\n" +
                                            "  \"status\": \"IN_PROGRESS\"\n" +
                                            "}"
                            ))
            )
            @RequestBody UserCourseProgressRequestDTO dto) {
        return ResponseEntity.ok(userProgressService.upsertCourseProgress(dto));
    }

    @GetMapping("/courses/{userId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    @Operation(summary = "Danh sách tiến độ khóa học của user")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Danh sách tiến độ khóa học",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = UserCourseProgressResponseDTO.class)))
            ),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền", content = @Content),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống", content = @Content)
    })
    public ResponseEntity<List<UserCourseProgressResponseDTO>> getCourseProgressByUser(
            @Parameter(description = "ID người dùng") @PathVariable Long userId) {
        return ResponseEntity.ok(userProgressService.getCourseProgressByUser(userId));
    }

    // ===== Session =====
    @PostMapping("/sessions")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    @Operation(summary = "Cập nhật tiến độ session")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tiến độ session sau khi cập nhật",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserSessionProgressResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "SessionProgressExample",
                                    value = "{\n" +
                                            "  \"id\": 1,\n" +
                                            "  \"userId\": 5,\n" +
                                            "  \"userName\": \"Nguyễn Văn A\",\n" +
                                            "  \"sessionId\": 7,\n" +
                                            "  \"sessionTitle\": \"Session 1: Giới thiệu Spring Boot\",\n" +
                                            "  \"courseId\": 3,\n" +
                                            "  \"courseTitle\": \"Spring Boot Fundamentals\",\n" +
                                            "  \"status\": \"IN_PROGRESS\",\n" +
                                            "  \"progressPercent\": 50.00,\n" +
                                            "  \"startedAt\": \"2025-01-05T08:00:00\"\n" +
                                            "}"
                            ))),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền", content = @Content),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống", content = @Content)
    })
    public ResponseEntity<UserSessionProgressResponseDTO> upsertSessionProgress(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Thông tin tiến độ session cần cập nhật",
                    content = @Content(schema = @Schema(implementation = UserSessionProgressRequestDTO.class),
                            examples = @ExampleObject(
                                    name = "SessionProgressRequest",
                                    value = "{\n" +
                                            "  \"userId\": 5,\n" +
                                            "  \"sessionId\": 7,\n" +
                                            "  \"courseId\": 3,\n" +
                                            "  \"status\": \"IN_PROGRESS\",\n" +
                                            "  \"progressPercent\": 50.00\n" +
                                            "}"
                            ))
            )
            @RequestBody UserSessionProgressRequestDTO dto) {
        return ResponseEntity.ok(userProgressService.upsertSessionProgress(dto));
    }

    @GetMapping("/sessions/{userId}/courses/{courseId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    @Operation(summary = "Danh sách tiến độ session theo user + course")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Danh sách tiến độ session",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = UserSessionProgressResponseDTO.class)))
            ),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền", content = @Content),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống", content = @Content)
    })
    public ResponseEntity<List<UserSessionProgressResponseDTO>> getSessionProgress(
            @Parameter(description = "ID người dùng") @PathVariable Long userId,
            @Parameter(description = "ID khóa học") @PathVariable Long courseId) {
        return ResponseEntity.ok(userProgressService.getSessionProgressByUserAndCourse(userId, courseId));
    }

    // ===== Lesson =====
    @PostMapping("/lessons")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    @Operation(summary = "Cập nhật tiến độ lesson")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tiến độ lesson sau khi cập nhật",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserLessonProgressResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "LessonProgressExample",
                                    value = "{\n" +
                                            "  \"id\": 1,\n" +
                                            "  \"userId\": 5,\n" +
                                            "  \"userName\": \"Nguyễn Văn A\",\n" +
                                            "  \"lessonId\": 11,\n" +
                                            "  \"lessonTitle\": \"Bài 1: Giới thiệu Spring Boot\",\n" +
                                            "  \"type\": \"video\",\n" +
                                            "  \"sessionId\": 7,\n" +
                                            "  \"sessionTitle\": \"Session 1: Giới thiệu Spring Boot\",\n" +
                                            "  \"courseId\": 3,\n" +
                                            "  \"courseTitle\": \"Spring Boot Fundamentals\",\n" +
                                            "  \"status\": \"IN_PROGRESS\",\n" +
                                            "  \"progressPercent\": 80.00,\n" +
                                            "  \"startedAt\": \"2025-01-05T08:00:00\"\n" +
                                            "}"
                            ))),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền", content = @Content),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống", content = @Content)
    })
    public ResponseEntity<UserLessonProgressResponseDTO> upsertLessonProgress(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Thông tin tiến độ lesson cần cập nhật",
                    content = @Content(schema = @Schema(implementation = UserLessonProgressRequestDTO.class),
                            examples = @ExampleObject(
                                    name = "LessonProgressRequest",
                                    value = "{\n" +
                                            "  \"userId\": 5,\n" +
                                            "  \"lessonId\": 11,\n" +
                                            "  \"sessionId\": 7,\n" +
                                            "  \"courseId\": 3,\n" +
                                            "  \"type\": \"video\",\n" +
                                            "  \"status\": \"IN_PROGRESS\",\n" +
                                            "  \"progressPercent\": 80.00\n" +
                                            "}"
                            ))
            )
            @RequestBody UserLessonProgressRequestDTO dto) {
        return ResponseEntity.ok(userProgressService.upsertLessonProgress(dto));
    }

    @GetMapping("/lessons/{userId}/courses/{courseId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    @Operation(summary = "Danh sách tiến độ lesson theo user + course")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Danh sách tiến độ lesson",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = UserLessonProgressResponseDTO.class)))
            ),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền", content = @Content),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống", content = @Content)
    })
    public ResponseEntity<List<UserLessonProgressResponseDTO>> getLessonProgress(
            @Parameter(description = "ID người dùng") @PathVariable Long userId,
            @Parameter(description = "ID khóa học") @PathVariable Long courseId) {
        return ResponseEntity.ok(userProgressService.getLessonProgressByUserAndCourse(userId, courseId));
    }

    // ===== Roadmap (Assignment) Progress =====
    @PostMapping("/roadmaps")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    @Operation(summary = "Tạo/Cập nhật trạng thái lộ trình của user")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Trạng thái lộ trình sau khi cập nhật",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserRoadmapProgressResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "RoadmapProgressExample",
                                    value = "{\n" +
                                            "  \"id\": 1,\n" +
                                            "  \"userId\": 5,\n" +
                                            "  \"roadmapId\": 12,\n" +
                                            "  \"status\": \"IN_PROGRESS\",\n" +
                                            "  \"currentItemId\": 101,\n" +
                                            "  \"completedItems\": 3,\n" +
                                            "  \"totalItems\": 10\n" +
                                            "}"
                            ))),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền", content = @Content),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống", content = @Content)
    })
    public ResponseEntity<UserRoadmapProgressResponseDTO> upsertRoadmapProgress(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Thông tin trạng thái lộ trình cần cập nhật",
                    content = @Content(schema = @Schema(implementation = UserRoadmapProgressRequestDTO.class),
                            examples = @ExampleObject(
                                    name = "RoadmapProgressRequest",
                                    value = "{\n" +
                                            "  \"userId\": 5,\n" +
                                            "  \"roadmapId\": 12,\n" +
                                            "  \"status\": \"IN_PROGRESS\",\n" +
                                            "  \"currentItemId\": 101\n" +
                                            "}"
                            ))
            )
            @RequestBody UserRoadmapProgressRequestDTO dto) {
        return ResponseEntity.ok(userProgressService.upsertRoadmapProgress(dto));
    }

    @GetMapping("/roadmaps/{userId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    @Operation(summary = "Danh sách trạng thái lộ trình theo user")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Danh sách trạng thái lộ trình",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = UserRoadmapProgressResponseDTO.class)))
            ),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền", content = @Content),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống", content = @Content)
    })
    public ResponseEntity<List<UserRoadmapProgressResponseDTO>> getRoadmapProgressByUser(
            @Parameter(description = "ID người dùng") @PathVariable Long userId) {
        return ResponseEntity.ok(userProgressService.getRoadmapProgressByUser(userId));
    }

    @GetMapping("/roadmaps/{userId}/{roadmapId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    @Operation(summary = "Trạng thái lộ trình cụ thể theo user + roadmap")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Trạng thái lộ trình",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserRoadmapProgressResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền", content = @Content),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống", content = @Content)
    })
    public ResponseEntity<UserRoadmapProgressResponseDTO> getRoadmapProgressByUserAndRoadmap(
            @Parameter(description = "ID người dùng") @PathVariable Long userId,
            @Parameter(description = "ID roadmap (assignment)") @PathVariable Long roadmapId) {
        return ResponseEntity.ok(userProgressService.getRoadmapProgressByUserAndRoadmap(userId, roadmapId));
    }
}


