package com.ra.base_spring_boot.services.validate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Component
public class AttemptFileValidatorImpl implements AttemptFileValidator {

    @Value("${app.attempts.max-size-bytes:20971520}") // 20MB default
    private long maxSize;

    @Value("${app.attempts.allowed-ext:pdf,doc,docx,zip,jpg,jpeg,png}")
    private String allowedExtCsv;

    @Override
    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File rỗng");
        }
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File vượt quá dung lượng cho phép");
        }
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String safeExt = (ext == null ? "" : ext.toLowerCase());
        Set<String> allowed = Set.of(allowedExtCsv.toLowerCase().split(","));
        if (!allowed.contains(safeExt)) {
            throw new IllegalArgumentException("Định dạng file không được phép: " + safeExt);
        }
    }
}
