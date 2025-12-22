package com.ra.base_spring_boot.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ra.base_spring_boot.dto.upload.UploadResponseDTO;
import com.ra.base_spring_boot.services.IUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadServiceImpl implements IUploadService {

    private final Cloudinary cloudinary;

    @Override
    public UploadResponseDTO uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        try {
            String publicId = "uploads/images/" + UUID.randomUUID();
            Map<String, Object> result = (Map<String, Object>) cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "resource_type", "image",
                            "overwrite", false
                    )
            );
            String url = (String) result.get("secure_url");
            if (url == null || url.isBlank()) {
                url = (String) result.get("url");
            }
            return UploadResponseDTO.builder().url(url).build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image to Cloudinary", e);
        }
    }

    @Override
    public UploadResponseDTO uploadVideo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        try {
            String publicId = "uploads/videos/" + UUID.randomUUID();
            Map<String, Object> options = ObjectUtils.asMap(
                    "public_id", publicId,
                    "resource_type", "video",
                    "overwrite", false
            );

            Map<String, Object> result;
            long size = file.getSize();
            if (size > 100L * 1024 * 1024) { // >100MB dùng uploadLarge
                result = (Map<String, Object>) cloudinary.uploader().uploadLarge(file.getBytes(), options);
            } else {
                result = (Map<String, Object>) cloudinary.uploader().upload(file.getBytes(), options);
            }

            String url = (String) result.get("secure_url");
            if (url == null || url.isBlank()) {
                url = (String) result.get("url");
            }
            return UploadResponseDTO.builder().url(url).build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload video to Cloudinary", e);
        }
    }

    @Override
    public UploadResponseDTO uploadPdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        // Basic validation: allow application/pdf, or fallback to accept any (but prefer pdf)
        String contentType = file.getContentType();
        if (contentType != null && !contentType.equalsIgnoreCase("application/pdf")) {
            throw new IllegalArgumentException("Only PDF files are allowed");
        }
        try {
            String publicId = "uploads/pdfs/" + UUID.randomUUID() + ".pdf";
            Map<String, Object> options = ObjectUtils.asMap(
                    "public_id", publicId,
                    "resource_type", "raw",
                    "overwrite", false
            );

            Map<String, Object> result = (Map<String, Object>) cloudinary.uploader().upload(file.getBytes(), options);

            String url = (String) result.get("secure_url");
            if (url == null || url.isBlank()) {
                url = (String) result.get("url");
            }
            return UploadResponseDTO.builder().url(url).build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload PDF to Cloudinary", e);
        }
    }
}
