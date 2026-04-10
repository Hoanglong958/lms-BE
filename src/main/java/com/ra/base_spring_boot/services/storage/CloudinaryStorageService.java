package com.ra.base_spring_boot.services.storage;

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
public class CloudinaryStorageService implements StorageService {

    private final Cloudinary cloudinary;

    public CloudinaryStorageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public String storeImage(MultipartFile file) {
        String folder = "uploads/images/" + LocalDate.now();
        String publicId = UUID.randomUUID().toString();
        try {
            Map<String, Object> options = Map.of(
                    "folder", folder,
                    "public_id", publicId,
                    "resource_type", "image",
                    "overwrite", true);
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), options);
            Object secureUrl = result.get("secure_url");
            Object url = result.get("url");
            return secureUrl != null ? secureUrl.toString() : (url != null ? url.toString() : null);
        } catch (IOException e) {
            throw new RuntimeException("Upload ảnh lên Cloudinary thất bại", e);
        }
    }

    @Override
    public String storeVideo(MultipartFile file) {
        String folder = "uploads/videos/" + LocalDate.now();
        String publicId = UUID.randomUUID().toString();
        try {
            Map<String, Object> options = Map.of(
                    "folder", folder,
                    "public_id", publicId,
                    "resource_type", "video",
                    "overwrite", true);

            Map<?, ?> result;
            long size = file.getSize();
            if (size > 100L * 1024 * 1024) {
                result = cloudinary.uploader().uploadLarge(file.getBytes(), options);
            } else {
                result = cloudinary.uploader().upload(file.getBytes(), options);
            }

            Object secureUrl = result.get("secure_url");
            Object url = result.get("url");
            return secureUrl != null ? secureUrl.toString() : (url != null ? url.toString() : null);
        } catch (IOException e) {
            throw new RuntimeException("Upload video lên Cloudinary thất bại", e);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        String folder = "uploads/files/" + LocalDate.now();
        String publicId = UUID.randomUUID().toString();
        try {
            Map<String, Object> options = Map.of(
                    "folder", folder,
                    "public_id", publicId,
                    "resource_type", "auto", // "auto" lets Cloudinary detect the type (image, video, raw)
                    "overwrite", true);

            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), options);
            Object secureUrl = result.get("secure_url");
            Object url = result.get("url");
            return secureUrl != null ? secureUrl.toString() : (url != null ? url.toString() : null);
        } catch (IOException e) {
            throw new RuntimeException("Upload file lên Cloudinary thất bại", e);
        }
    }
}
