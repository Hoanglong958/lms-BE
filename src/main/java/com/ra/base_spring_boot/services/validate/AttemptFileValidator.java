package com.ra.base_spring_boot.services.validate;

import org.springframework.web.multipart.MultipartFile;

public interface AttemptFileValidator {
    void validate(MultipartFile file);
}
