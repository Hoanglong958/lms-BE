package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.upload.UploadResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface IUploadService {
    UploadResponseDTO uploadImage(MultipartFile file);
    UploadResponseDTO uploadVideo(MultipartFile file);
    /**
     * Upload a PDF (or other raw file) and return a public URL.
     */
    UploadResponseDTO uploadPdf(MultipartFile file);
} 
