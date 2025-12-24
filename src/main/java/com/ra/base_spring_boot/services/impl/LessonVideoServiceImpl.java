package com.ra.base_spring_boot.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ra.base_spring_boot.dto.LessonVideo.LessonVideoRequestDTO;
import com.ra.base_spring_boot.dto.LessonVideo.LessonVideoResponseDTO;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.Lesson;
import com.ra.base_spring_boot.model.LessonVideo;
import com.ra.base_spring_boot.model.constants.LessonType;
import com.ra.base_spring_boot.repository.ILessonRepository;
import com.ra.base_spring_boot.repository.ILessonVideoRepository;
import com.ra.base_spring_boot.services.ILessonVideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LessonVideoServiceImpl implements ILessonVideoService {

    private final ILessonVideoRepository lessonVideoRepository;
    private final ILessonRepository lessonRepository;
    private final Cloudinary cloudinary;

    @Override
    public List<LessonVideoResponseDTO> getByLesson(Long lessonId) {
        List<LessonVideo> videos = lessonVideoRepository
                .findByLesson_Id(java.util.Objects.requireNonNull(lessonId, "lessonId must not be null"));
        return videos.stream().map(this::mapToResponse).toList();
    }

    @Override
    public LessonVideoResponseDTO getById(Long id) {
        LessonVideo video = lessonVideoRepository.findById(java.util.Objects.requireNonNull(id, "id must not be null"))
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy video với id = " + id));
        return mapToResponse(video);
    }

    @Override
    public LessonVideoResponseDTO create(LessonVideoRequestDTO dto) {
        Lesson lesson = lessonRepository
                .findById(java.util.Objects.requireNonNull(dto.getLessonId(), "lessonId must not be null"))
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy bài học với id = " + dto.getLessonId()));

        // Validate lesson type must be VIDEO
        if (lesson.getType() != LessonType.VIDEO) {
            throw new HttpBadRequest("Lesson type must be VIDEO để tạo video cho bài học này");
        }

        Integer nextOrderIndex = lessonVideoRepository.findMaxOrderIndexByLessonId(lesson.getId());
        nextOrderIndex = (nextOrderIndex == null ? 0 : nextOrderIndex) + 1;

        LessonVideo video = LessonVideo.builder()
                .lesson(lesson)
                .title(dto.getTitle())
                .videoUrl(dto.getVideoUrl())
                .description(dto.getDescription())
                .durationSeconds(dto.getDurationSeconds())
                .orderIndex(nextOrderIndex)
                .build();

        // Tự động lấy thời lượng nếu chưa có
        if (video.getDurationSeconds() == null || video.getDurationSeconds() == 0) {
            video.setDurationSeconds(fetchDurationFromCloudinary(video.getVideoUrl()));
        }

        LessonVideo saved = lessonVideoRepository
                .save(java.util.Objects.requireNonNull(video, "video must not be null"));
        return mapToResponse(saved);
    }

    @Override
    public LessonVideoResponseDTO update(Long id, LessonVideoRequestDTO dto) {
        LessonVideo video = lessonVideoRepository.findById(java.util.Objects.requireNonNull(id, "id must not be null"))
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy video với id = " + id));

        if (dto.getTitle() != null)
            video.setTitle(dto.getTitle());
        if (dto.getVideoUrl() != null) {
            video.setVideoUrl(dto.getVideoUrl());
            // Nếu đổi URL thì cũng nên reset/re-fetch duration nếu dto không gửi duration
            // mới
            if (dto.getDurationSeconds() == null) {
                video.setDurationSeconds(fetchDurationFromCloudinary(dto.getVideoUrl()));
            }
        }
        if (dto.getDurationSeconds() != null)
            video.setDurationSeconds(dto.getDurationSeconds());
        if (dto.getDescription() != null)
            video.setDescription(dto.getDescription());

        LessonVideo updated = lessonVideoRepository
                .save(java.util.Objects.requireNonNull(video, "video must not be null"));
        return mapToResponse(updated);
    }

    private Integer fetchDurationFromCloudinary(String videoUrl) {
        log.info("[Cloudinary] Fetching duration for URL: {}", videoUrl);
        if (videoUrl == null || videoUrl.isBlank() || !videoUrl.contains("cloudinary.com")) {
            log.warn("[Cloudinary] Not a Cloudinary URL or empty, skipping duration fetch.");
            return null;
        }

        try {
            // Sử dụng Regex để trích xuất publicId chính xác hơn từ URL Cloudinary
            // Pattern này hỗ trợ cả version (v123...) và các transformations
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                    ".*/(?:video|upload)/(?:v\\d+/)?(?:[^/]+/)*?([^./?#]+(?:/[^./?#]+)*)(?:\\.[a-z0-9]+)?(?:[?#].*)?$");
            java.util.regex.Matcher matcher = pattern.matcher(videoUrl);

            String publicId = null;
            if (matcher.find()) {
                publicId = matcher.group(1);
            } else {
                // Fallback: Lấy phần cuối của URL
                String temp = videoUrl;
                if (temp.contains("/upload/"))
                    temp = temp.substring(temp.lastIndexOf("/upload/") + 8);
                else if (temp.contains("/video/"))
                    temp = temp.substring(temp.lastIndexOf("/video/") + 7);
                else
                    temp = temp.substring(temp.lastIndexOf("/") + 1);

                if (temp.contains("."))
                    temp = temp.substring(0, temp.lastIndexOf("."));
                publicId = temp;
            }

            log.info("[Cloudinary] Extracted PublicId: {}", publicId);

            // Gọi Admin API để lấy thông tin chi tiết (bao gồm duration)
            // Lưu ý: api_key và api_secret phải được cấu hình đúng
            Map<String, Object> details = (Map<String, Object>) cloudinary.api().resource(publicId,
                    com.cloudinary.utils.ObjectUtils.asMap("resource_type", "video"));

            if (details != null && details.containsKey("duration")) {
                Object durationObj = details.get("duration");
                Double duration = Double.parseDouble(durationObj.toString());
                log.info("[Cloudinary] Found duration: {} seconds", duration);
                return (int) Math.round(duration);
            } else {
                log.warn("[Cloudinary] Resource found but duration is missing for {}. Full response: {}", publicId,
                        details);
            }
        } catch (Exception e) {
            log.error("[Cloudinary] Failed to fetch duration for {}. Error: {} - {}", videoUrl,
                    e.getClass().getSimpleName(), e.getMessage());
        }
        return null;
    }

    @Override
    public void delete(Long id) {
        LessonVideo video = lessonVideoRepository.findById(java.util.Objects.requireNonNull(id, "id must not be null"))
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy video với id = " + id));
        lessonVideoRepository.delete(java.util.Objects.requireNonNull(video, "video must not be null"));
    }

    private LessonVideoResponseDTO mapToResponse(LessonVideo video) {
        return LessonVideoResponseDTO.builder()
                .videoId(video.getId())
                .title(video.getTitle())
                .videoUrl(video.getVideoUrl())
                .durationSeconds(video.getDurationSeconds())
                .description(video.getDescription())
                .lessonId(video.getLesson().getId())
                .lessonTitle(video.getLesson().getTitle())
                .createdAt(video.getCreatedAt())
                .updatedAt(video.getUpdatedAt())
                .build();
    }
}
