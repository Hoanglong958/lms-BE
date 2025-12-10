package com.ra.base_spring_boot.services.storage;

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
public class LocalStorageService implements StorageService {

    @Value("${app.upload.root:uploads}")
    private String uploadRoot;

    @Override
    public String storeImage(MultipartFile file) {
        return store(file, "images");
    }

    @Override
    public String storeVideo(MultipartFile file) {
        return store(file, "videos");
    }

    private String store(MultipartFile file, String type) {
        try {
            String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String safeExt = (ext == null || ext.isBlank()) ? "bin" : ext.toLowerCase();
            String dateDir = LocalDate.now().toString();
            String newName = UUID.randomUUID() + "." + safeExt;
            Path targetDir = Paths.get(uploadRoot, type, dateDir);
            Files.createDirectories(targetDir);
            Path target = targetDir.resolve(newName);
            file.transferTo(java.util.Objects.requireNonNull(target, "target must not be null"));
            // public URL mapping via StaticResourceConfig -> /uploads/**
            return "/uploads/" + type + "/" + dateDir + "/" + newName;
        } catch (IOException e) {
            throw new RuntimeException("Lưu file thất bại", e);
        }
    }
}
