package com.ra.base_spring_boot.services.upload;

import org.springframework.web.multipart.MultipartFile;

public interface AttemptAttachmentStorage {
    String storeAttemptFile(Long attemptId, MultipartFile file);
}
