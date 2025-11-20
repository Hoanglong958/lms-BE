package com.ra.base_spring_boot.services.validator;

import com.ra.base_spring_boot.exception.HttpBadRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Component
public class FileValidator {
    private static final long MAX_IMAGE_BYTES = 5 * 1024 * 1024; // 5MB
    private static final long MAX_VIDEO_BYTES = 200 * 1024 * 1024; // 200MB

    private static final Set<String> IMAGE_TYPES = Set.of(
            "image/png", "image/jpeg", "image/jpg", "image/webp", "image/gif"
    );
    private static final Set<String> VIDEO_TYPES = Set.of(
            "video/mp4", "video/quicktime", "video/webm", "video/x-msvideo"
    );

    public void validateImage(MultipartFile file) {
        validateCommon(file);
        if (!IMAGE_TYPES.contains(file.getContentType())) {
            throw new HttpBadRequest("Định dạng ảnh không hợp lệ. Hỗ trợ: PNG, JPEG, WEBP, GIF");
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new HttpBadRequest("Kích thước ảnh vượt quá 5MB");
        }
    }

    public void validateVideo(MultipartFile file) {
        validateCommon(file);
        if (!VIDEO_TYPES.contains(file.getContentType())) {
            throw new HttpBadRequest("Định dạng video không hợp lệ. Hỗ trợ: MP4, MOV, WEBM, AVI");
        }
        if (file.getSize() > MAX_VIDEO_BYTES) {
            throw new HttpBadRequest("Kích thước video vượt quá 200MB");
        }
    }

    private void validateCommon(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new HttpBadRequest("File rỗng hoặc không tồn tại");
        }
    }
}
