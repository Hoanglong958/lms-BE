package com.ra.base_spring_boot.services.upload;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class AttemptAttachmentLocalStorageImpl implements AttemptAttachmentStorage {

    @Value("${app.upload.root:uploads}")
    private String uploadRoot;

    @Override
    public String storeAttemptFile(Long attemptId, MultipartFile file) {
        try {
            String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String safeExt = (ext == null || ext.isBlank()) ? "bin" : ext.toLowerCase();
            String dateDir = LocalDate.now().toString();
            String newName = UUID.randomUUID() + "." + safeExt;
            Path targetDir = Paths.get(uploadRoot, "attempts", String.valueOf(attemptId), dateDir);
            Files.createDirectories(targetDir);
            Path target = targetDir.resolve(newName);
            file.transferTo(java.util.Objects.requireNonNull(target, "target must not be null"));
            return "/uploads/attempts/" + attemptId + "/" + dateDir + "/" + newName;
        } catch (IOException e) {
            throw new RuntimeException("Lưu file đính kèm thất bại", e);
        }
    }
}
