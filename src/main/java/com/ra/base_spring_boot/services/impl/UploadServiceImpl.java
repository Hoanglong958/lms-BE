package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.upload.UploadResponseDTO;
import com.ra.base_spring_boot.services.IUploadService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class UploadServiceImpl implements IUploadService {
    @Override
    public UploadResponseDTO uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        String url = "/uploads/images/" + UUID.randomUUID();
        return UploadResponseDTO.builder().url(url).build();
    }

    @Override
    public UploadResponseDTO uploadVideo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        String url = "/uploads/videos/" + UUID.randomUUID();
        return UploadResponseDTO.builder().url(url).build();
    }
}
