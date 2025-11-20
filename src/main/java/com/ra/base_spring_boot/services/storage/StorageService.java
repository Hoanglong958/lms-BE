package com.ra.base_spring_boot.services.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String storeImage(MultipartFile file);
    String storeVideo(MultipartFile file);
}
