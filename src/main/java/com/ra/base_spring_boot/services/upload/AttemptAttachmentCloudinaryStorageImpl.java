package com.ra.base_spring_boot.services.upload;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Service
@Primary
public class AttemptAttachmentCloudinaryStorageImpl implements AttemptAttachmentStorage {

    private final Cloudinary cloudinary;

    public AttemptAttachmentCloudinaryStorageImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public String storeAttemptFile(Long attemptId, MultipartFile file) {
        String folder = "uploads/attempts/" + attemptId + "/" + LocalDate.now();
        String publicId = UUID.randomUUID().toString();
        try {
            Map<String, Object> options = Map.of(
                    "folder", folder,
                    "public_id", publicId,
                    "resource_type", "auto",
                    "overwrite", true
            );
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), options);
            Object secureUrl = result.get("secure_url");
            Object url = result.get("url");
            return secureUrl != null ? secureUrl.toString() : (url != null ? url.toString() : null);
        } catch (IOException e) {
            throw new RuntimeException("Upload file đính kèm lên Cloudinary thất bại", e);
        }
    }
}
