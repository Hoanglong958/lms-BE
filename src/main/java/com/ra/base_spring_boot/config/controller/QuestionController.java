package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.dto.Question.QuestionRequestDTO;
import com.ra.base_spring_boot.dto.Question.QuestionResponseDTO;
import com.ra.base_spring_boot.services.IQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
@Tag(name = "08 - Questions", description = "Quản lý câu hỏi")
public class QuestionController {

    private final IQuestionService questionService;

    // ================== GET QUESTIONS (PAGING + SEARCH) ==================
    @GetMapping("/page")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @Operation(
            summary = "Lấy danh sách câu hỏi (phân trang + tìm kiếm)",
            description = "Hỗ trợ phân trang, tìm theo nội dung câu hỏi và category"
    )
    public ResponseEntity<?> getQuestionsPaging(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category
    ) {
        return ResponseEntity.ok(
                questionService.getQuestions(page, size, keyword, category)
        );
    }

    // ================== GET BY ID ==================
    @GetMapping("/detail")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @Operation(
            summary = "Lấy chi tiết câu hỏi",
            description = "Cho phép ADMIN và USER xem chi tiết một câu hỏi"
    )
    @ApiResponse(responseCode = "200", description = "Lấy chi tiết thành công")
    public ResponseEntity<QuestionResponseDTO> getQuestionById(
            @RequestParam Long id
    ) {
        return ResponseEntity.ok(questionService.getById(id));
    }

    // ================== CREATE ==================
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
            summary = "Tạo câu hỏi mới",
            description = "Chỉ ADMIN được phép tạo mới câu hỏi"
    )
    @ApiResponse(responseCode = "201", description = "Tạo câu hỏi thành công")
    public ResponseEntity<QuestionResponseDTO> createQuestion(
            @Valid @RequestBody QuestionRequestDTO requestDTO
    ) {
        QuestionResponseDTO response = questionService.create(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ================== CREATE BULK ==================
    @PostMapping("/bulk")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
            summary = "Tạo nhiều câu hỏi",
            description = "Chỉ ADMIN được phép tạo nhiều câu hỏi cùng lúc"
    )
    @ApiResponse(responseCode = "201", description = "Tạo nhiều câu hỏi thành công")
    public ResponseEntity<List<QuestionResponseDTO>> createQuestionsBulk(
            @Valid @RequestBody List<QuestionRequestDTO> requests
    ) {
        List<QuestionResponseDTO> responses = questionService.createBulk(requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    // ================== UPDATE ==================
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
            summary = "Cập nhật câu hỏi",
            description = "Chỉ ADMIN được phép chỉnh sửa câu hỏi"
    )
    @ApiResponse(responseCode = "200", description = "Cập nhật thành công")
    public ResponseEntity<QuestionResponseDTO> updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody QuestionRequestDTO requestDTO
    ) {
        return ResponseEntity.ok(questionService.update(id, requestDTO));
    }

    // ================== DELETE ==================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
            summary = "Xóa câu hỏi",
            description = "Chỉ ADMIN được phép xóa câu hỏi"
    )
    @ApiResponse(responseCode = "204", description = "Xóa thành công")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable Long id
    ) {
        questionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
