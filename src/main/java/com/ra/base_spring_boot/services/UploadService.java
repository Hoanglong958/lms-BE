package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.upload.UploadResponseDTO;
import com.ra.base_spring_boot.services.storage.StorageService;
import com.ra.base_spring_boot.services.validator.FileValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UploadService {
    private final StorageService storageService;
    private final FileValidator fileValidator;

    public UploadResponseDTO uploadImage(MultipartFile file) {
        fileValidator.validateImage(file);
        String url = storageService.storeImage(file);
        return UploadResponseDTO.builder().url(url).build();
    }

    public UploadResponseDTO uploadVideo(MultipartFile file) {
        fileValidator.validateVideo(file);
        String url = storageService.storeVideo(file);
        return UploadResponseDTO.builder().url(url).build();
    }
}
