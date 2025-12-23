package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.dto.Question.QuestionRequestDTO;
import com.ra.base_spring_boot.dto.Question.QuestionResponseDTO;
import com.ra.base_spring_boot.services.IQuestionService;
import com.ra.base_spring_boot.services.IUploadService;
import com.ra.base_spring_boot.utils.ExcelHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
@Tag(name = "08 - Questions", description = "Quản lý câu hỏi")
public class QuestionController {

        private final IQuestionService questionService;
        private final IUploadService uploadService;

        // ================== GET QUESTIONS (PAGING + SEARCH) ==================
        @GetMapping("/page")
        @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
        @Operation(summary = "Lấy danh sách câu hỏi (phân trang + tìm kiếm)", description = "Hỗ trợ phân trang, tìm theo nội dung câu hỏi và category")
        public ResponseEntity<?> getQuestionsPaging(
                        @RequestParam(defaultValue = "0") Integer page,
                        @RequestParam(defaultValue = "10") Integer size,
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) String category) {
                return ResponseEntity.ok(
                                questionService.getQuestions(page, size, keyword, category));
        }

        // ================== GET BY ID ==================
        @GetMapping("/detail")
        @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
        @Operation(summary = "Lấy chi tiết câu hỏi", description = "Cho phép ADMIN và USER xem chi tiết một câu hỏi")
        @ApiResponse(responseCode = "200", description = "Lấy chi tiết thành công")
        public ResponseEntity<QuestionResponseDTO> getQuestionById(
                        @RequestParam Long id) {
                return ResponseEntity.ok(questionService.getById(id));
        }

        // ================== CREATE ==================
        @PostMapping
        @PreAuthorize("hasAuthority('ROLE_ADMIN')")
        @Operation(summary = "Tạo câu hỏi mới", description = "Chỉ ADMIN được phép tạo mới câu hỏi")
        @ApiResponse(responseCode = "201", description = "Tạo câu hỏi thành công")
        public ResponseEntity<QuestionResponseDTO> createQuestion(
                        @Valid @RequestBody QuestionRequestDTO requestDTO) {
                QuestionResponseDTO response = questionService.create(requestDTO);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        // ================== CREATE BULK ==================
        @PostMapping("/bulk")
        @PreAuthorize("hasAuthority('ROLE_ADMIN')")
        @Operation(summary = "Tạo nhiều câu hỏi", description = "Chỉ ADMIN được phép tạo nhiều câu hỏi cùng lúc")
        @ApiResponse(responseCode = "201", description = "Tạo nhiều câu hỏi thành công")
        public ResponseEntity<List<QuestionResponseDTO>> createQuestionsBulk(
                        @Valid @RequestBody List<QuestionRequestDTO> requests) {
                List<QuestionResponseDTO> responses = questionService.createBulk(requests);
                return ResponseEntity.status(HttpStatus.CREATED).body(responses);
        }

        // ================== UPDATE ==================
        @PutMapping("/{id}")
        @PreAuthorize("hasAuthority('ROLE_ADMIN')")
        @Operation(summary = "Cập nhật câu hỏi", description = "Chỉ ADMIN được phép chỉnh sửa câu hỏi")
        @ApiResponse(responseCode = "200", description = "Cập nhật thành công")
        public ResponseEntity<QuestionResponseDTO> updateQuestion(
                        @PathVariable Long id,
                        @Valid @RequestBody QuestionRequestDTO requestDTO) {
                return ResponseEntity.ok(questionService.update(id, requestDTO));
        }

        // ================== UPLOAD EXCEL ==================
        @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("hasAuthority('ROLE_ADMIN')")
        @Operation(summary = "Tải lên câu hỏi từ file Excel (Multipart)", description = "Lưu file lên Cloudinary và tạo câu hỏi trong DB. Định dạng: Cột 0: Category, Cột 1: Question, Cột 2-5: Options, Cột 6: Correct Answer, Cột 7: Explanation")
        @ApiResponse(responseCode = "201", description = "Tải lên thành công")
        @ApiResponse(responseCode = "400", description = "File không đúng định dạng")
        public ResponseEntity<?> uploadQuestions(
                        @RequestParam("file") MultipartFile file) {
                if (!ExcelHelper.hasExcelFormat(file)) {
                        return ResponseEntity.badRequest().body("Please upload an excel file!");
                }

                try {
                        // 1. Upload to Cloudinary
                        String fileUrl = uploadService.uploadExcel(file).getUrl();

                        // 2. Parse and Create Questions
                        return processExcelAndCreateQuestions(file.getInputStream(), fileUrl);
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                                        .body("Could not upload the file: " + file.getOriginalFilename() + "!");
                }
        }

        @PostMapping(value = "/upload", consumes = MediaType.APPLICATION_JSON_VALUE)
        @PreAuthorize("hasAuthority('ROLE_ADMIN')")
        @Operation(summary = "Tải lên câu hỏi từ URL (JSON)", description = "Nhận JSON { \"file\": \"url_excel\" } và tạo câu hỏi trong DB")
        public ResponseEntity<?> uploadQuestionsJson(@RequestBody Map<String, String> body) {
                String fileUrl = body.get("file");
                if (fileUrl == null)
                        fileUrl = body.get("url");

                if (fileUrl == null || fileUrl.isBlank()) {
                        return ResponseEntity.badRequest().body("Please provide a valid URL in 'file' or 'url' field");
                }
                return importQuestionsFromUrl(fileUrl);
        }

        @PostMapping("/import-url")
        @PreAuthorize("hasAuthority('ROLE_ADMIN')")
        @Operation(summary = "Nhập câu hỏi từ URL file Excel", description = "Nhận link Excel (ví dụ từ Cloudinary) và tạo câu hỏi trong DB")
        @ApiResponse(responseCode = "201", description = "Nhập câu hỏi thành công")
        public ResponseEntity<?> importQuestionsFromUrl(@RequestParam("url") String fileUrl) {
                try {
                        URL url = new URL(fileUrl);
                        try (InputStream is = url.openStream()) {
                                return processExcelAndCreateQuestions(is, fileUrl);
                        }
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body("Could not process the file from URL: " + fileUrl);
                }
        }

        private ResponseEntity<?> processExcelAndCreateQuestions(InputStream is, String fileUrl) {
                try {
                        List<QuestionRequestDTO> requests = ExcelHelper.excelToQuestions(is);
                        List<QuestionResponseDTO> responses = questionService.createBulk(requests);

                        Map<String, Object> result = new HashMap<>();
                        result.put("url", fileUrl);
                        result.put("questions", responses);

                        return ResponseEntity.status(HttpStatus.CREATED).body(result);
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body("Error parsing Excel content: " + e.getMessage());
                }
        }

        // ================== DELETE ==================
        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('ROLE_ADMIN')")
        @Operation(summary = "Xóa câu hỏi", description = "Chỉ ADMIN được phép xóa câu hỏi")
        @ApiResponse(responseCode = "204", description = "Xóa thành công")
        public ResponseEntity<Void> deleteQuestion(
                        @PathVariable Long id) {
                questionService.delete(id);
                return ResponseEntity.noContent().build();
        }
}
