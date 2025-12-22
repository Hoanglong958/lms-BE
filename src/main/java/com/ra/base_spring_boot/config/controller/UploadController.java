package com.ra.base_spring_boot.config.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ra.base_spring_boot.dto.upload.UploadResponseDTO;
import com.ra.base_spring_boot.services.IUploadService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/uploads")
@RequiredArgsConstructor
@Tag(name = "24 - Uploads", description = "Upload ảnh/video, trả về URL để sử dụng trong các chức năng")
public class UploadController {

    private final IUploadService uploadService;

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload ảnh", description = "Nhận MultipartFile, validate và lưu, trả về URL public")
    public ResponseEntity<UploadResponseDTO> uploadImage(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(uploadService.uploadImage(file));
    }

    @PostMapping(value = "/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload video", description = "Nhận MultipartFile, validate và lưu, trả về URL public")
    public ResponseEntity<UploadResponseDTO> uploadVideo(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(uploadService.uploadVideo(file));
    }

    @PostMapping(value = "/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload PDF", description = "Nhận file PDF, validate và lưu lên Cloudinary, trả về URL public")
    public ResponseEntity<UploadResponseDTO> uploadPdf(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(uploadService.uploadPdf(file));
    }
}
