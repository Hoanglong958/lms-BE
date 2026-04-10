package com.ra.base_spring_boot.services.course.impl;

import com.ra.base_spring_boot.dto.Session.SessionRequestDTO;
import com.ra.base_spring_boot.dto.Session.SessionResponseDTO;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.Course;
import com.ra.base_spring_boot.model.Session;
import com.ra.base_spring_boot.repository.course.ICourseRepository;
import com.ra.base_spring_boot.repository.course.ISessionRepository;
import com.ra.base_spring_boot.services.course.ISessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements ISessionService {

    private final ISessionRepository sessionRepository;
    private final ICourseRepository courseRepository;

    @Override
    public List<SessionResponseDTO> getByCourse(Long courseId) {
        List<Session> sessions = sessionRepository.findByCourse_IdOrderByOrderIndexAsc(java.util.Objects.requireNonNull(courseId, "courseId must not be null"));
        return sessions.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public SessionResponseDTO getById(Long id) {
        Session session = sessionRepository.findById(java.util.Objects.requireNonNull(id, "id must not be null"))
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy buổi học với id = " + id));
        return mapToResponse(session);
    }

    @Override
    public SessionResponseDTO create(SessionRequestDTO dto) {
        Course course = courseRepository.findById(java.util.Objects.requireNonNull(dto.getCourseId(), "courseId must not be null"))
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy khóa học với id = " + dto.getCourseId()));

        Session session = Session.builder()
                .title(dto.getTitle())
                .orderIndex(dto.getOrderIndex())
                .course(course)
                .build();

        Session saved = sessionRepository.save(java.util.Objects.requireNonNull(session, "session must not be null"));
        return mapToResponse(saved);
    }

    @Override
    public void delete(Long id) {
        Session session = sessionRepository.findById(java.util.Objects.requireNonNull(id, "id must not be null"))
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy buổi học với id = " + id));
        sessionRepository.delete(java.util.Objects.requireNonNull(session, "session must not be null"));
    }

    @Override
    public SessionResponseDTO update(Long id, SessionRequestDTO dto) {
        Session session = sessionRepository.findById(java.util.Objects.requireNonNull(id, "id must not be null"))
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy buổi học với id = " + id));

        if (dto.getTitle() != null) session.setTitle(dto.getTitle());
        if (dto.getOrderIndex() != null) session.setOrderIndex(dto.getOrderIndex());

        Session saved = sessionRepository.save(java.util.Objects.requireNonNull(session, "session must not be null"));
        return mapToResponse(saved);
    }

    private SessionResponseDTO mapToResponse(Session session) {
        return SessionResponseDTO.builder()
                .id(session.getId())
                .title(session.getTitle())
                .orderIndex(session.getOrderIndex())
                .courseId(session.getCourse().getId())
                .courseName(session.getCourse().getTitle())
                .createdAt(session.getCreatedAt())
                .build();
    }
}
