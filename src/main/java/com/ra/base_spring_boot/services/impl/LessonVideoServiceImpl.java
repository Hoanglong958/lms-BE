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
        if (videoUrl == null || !videoUrl.contains("cloudinary.com")) {
            log.warn("[Cloudinary] Not a Cloudinary URL, skipping duration fetch.");
            return null;
        }

        try {
            // Extract publicId using a more robust approach
            String publicId = videoUrl;

            // 1. Get everything after /upload/ (or /fetch/, /video/, etc if needed)
            if (publicId.contains("/upload/")) {
                publicId = publicId.substring(publicId.lastIndexOf("/upload/") + 8);
            } else if (publicId.contains("/video/")) {
                publicId = publicId.substring(publicId.lastIndexOf("/video/") + 7);
            }

            // 2. Remove version (e.g., v123456789/)
            if (publicId.matches("v\\d+/.*")) {
                publicId = publicId.substring(publicId.indexOf("/") + 1);
            }

            // 3. Remove extension (.mp4, .mp1, .mov, etc.)
            if (publicId.contains(".")) {
                publicId = publicId.substring(0, publicId.lastIndexOf("."));
            }

            log.info("[Cloudinary] Final PublicId extracted: {}", publicId);

            // 4. Call Admin API (requires api_secret)
            Map<String, Object> details = (Map<String, Object>) cloudinary.api().resource(publicId,
                    ObjectUtils.asMap("resource_type", "video"));

            if (details != null && details.containsKey("duration")) {
                Object durationObj = details.get("duration");
                Double duration = Double.parseDouble(durationObj.toString());
                log.info("[Cloudinary] Found duration: {} seconds for {}", duration, publicId);
                return (int) Math.round(duration);
            } else {
                log.warn("[Cloudinary] Duration not found in details for {}. Keys present: {}", publicId,
                        details != null ? details.keySet() : "null");
            }
        } catch (Exception e) {
            log.error("[Cloudinary] Error fetching duration for {}: {}", videoUrl, e.getMessage());
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
