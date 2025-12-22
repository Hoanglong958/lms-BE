package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.LessonDocument.LessonDocumentRequestDTO;
import com.ra.base_spring_boot.dto.LessonDocument.LessonDocumentResponseDTO;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.Lesson;
import com.ra.base_spring_boot.model.LessonDocument;
import com.ra.base_spring_boot.model.constants.LessonType;
import com.ra.base_spring_boot.repository.ILessonDocumentRepository;
import com.ra.base_spring_boot.repository.ILessonRepository;
import com.ra.base_spring_boot.services.ILessonDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonDocumentServiceImpl implements ILessonDocumentService {

    private final ILessonDocumentRepository documentRepository;
    private final ILessonRepository lessonRepository;

    @Override
    public List<LessonDocumentResponseDTO> getByLesson(Long lessonId) {
        List<LessonDocument> docs = documentRepository.findByLesson_IdOrderBySortOrderAsc(java.util.Objects.requireNonNull(lessonId, "lessonId must not be null"));
        return docs.stream().map(this::mapToResponse).toList();
    }

    @Override
    public LessonDocumentResponseDTO getById(Long id) {
        LessonDocument doc = documentRepository.findById(java.util.Objects.requireNonNull(id, "id must not be null"))
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy document với id = " + id));
        return mapToResponse(doc);
    }

    @Override
    public LessonDocumentResponseDTO create(LessonDocumentRequestDTO dto) {
        Lesson lesson = lessonRepository.findById(java.util.Objects.requireNonNull(dto.getLessonId(), "lessonId must not be null"))
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy lesson với id = " + dto.getLessonId()));

        // Validate lesson type must be DOCUMENT
        if (lesson.getType() != LessonType.DOCUMENT) {
            throw new HttpBadRequest("Lesson type must be DOCUMENT để tạo document cho bài học này");
        }

        Integer sortOrder = dto.getSortOrder();
        if (sortOrder == null) {
            Integer currentMax = documentRepository.findMaxSortOrderByLessonId(lesson.getId());
            sortOrder = (currentMax == null ? 0 : currentMax) + 1;
        }

        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new HttpBadRequest("Tiêu đề không được để trống");
        }

        LessonDocument entity = LessonDocument.builder()
                .lesson(lesson)
                .title(dto.getTitle())
                .content(dto.getContent())
                .imageUrl(dto.getImageUrl())
                .videoUrl(dto.getVideoUrl())
                .pdfUrl(dto.getPdfUrl())
                .sortOrder(sortOrder)
                .build();

        LessonDocument saved = documentRepository.save(java.util.Objects.requireNonNull(entity, "document must not be null"));
        return mapToResponse(saved);
    }

    @Override
    public LessonDocumentResponseDTO update(Long id, LessonDocumentRequestDTO dto) {
        LessonDocument entity = documentRepository.findById(java.util.Objects.requireNonNull(id, "id must not be null"))
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy document với id = " + id));

        if (dto.getLessonId() != null && (entity.getLesson() == null || !dto.getLessonId().equals(entity.getLesson().getId()))) {
            Lesson lesson = lessonRepository.findById(java.util.Objects.requireNonNull(dto.getLessonId(), "lessonId must not be null"))
                    .orElseThrow(() -> new HttpBadRequest("Không tìm thấy lesson với id = " + dto.getLessonId()));

            // Validate reassignment respects lesson type DOCUMENT
            if (lesson.getType() != LessonType.DOCUMENT) {
                throw new HttpBadRequest("Lesson type must be DOCUMENT để gán document vào bài học này");
            }
            entity.setLesson(lesson);
        }
        if (dto.getTitle() != null) entity.setTitle(dto.getTitle());
        if (dto.getContent() != null) entity.setContent(dto.getContent());
        if (dto.getImageUrl() != null) entity.setImageUrl(dto.getImageUrl());
        if (dto.getVideoUrl() != null) entity.setVideoUrl(dto.getVideoUrl());
        if (dto.getPdfUrl() != null) entity.setPdfUrl(dto.getPdfUrl());
        if (dto.getSortOrder() != null) entity.setSortOrder(dto.getSortOrder());

        LessonDocument updated = documentRepository.save(java.util.Objects.requireNonNull(entity, "document must not be null"));
        return mapToResponse(updated);
    }

    @Override
    public void delete(Long id) {
        LessonDocument entity = documentRepository.findById(java.util.Objects.requireNonNull(id, "id must not be null"))
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy document với id = " + id));
        documentRepository.delete(java.util.Objects.requireNonNull(entity, "document must not be null"));
    }

    private LessonDocumentResponseDTO mapToResponse(LessonDocument doc) {
        return LessonDocumentResponseDTO.builder()
                .documentId(doc.getId())
                .lessonId(doc.getLesson().getId())
                .lessonTitle(doc.getLesson().getTitle())
                .title(doc.getTitle())
                .content(doc.getContent())
                .imageUrl(doc.getImageUrl())
                .videoUrl(doc.getVideoUrl())
                .sortOrder(doc.getSortOrder())
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }
}
