package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.SessionExercise.SessionExerciseRequestDTO;
import com.ra.base_spring_boot.dto.SessionExercise.SessionExerciseResponseDTO;
import com.ra.base_spring_boot.model.Session;
import com.ra.base_spring_boot.model.SessionExercise;
import com.ra.base_spring_boot.repository.ISessionExerciseRepository;
import com.ra.base_spring_boot.repository.ISessionRepository;
import com.ra.base_spring_boot.services.ISessionExerciseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SessionExerciseServiceImpl implements ISessionExerciseService {

    private final ISessionExerciseRepository sessionExerciseRepository;
    private final ISessionRepository sessionRepository;

    /**
     * Tạo mới bài tập trong session
     */
    @Override
    public SessionExerciseResponseDTO create(SessionExerciseRequestDTO request) {
        Session session = sessionRepository.findById(java.util.Objects.requireNonNull(request.getSessionId(), "sessionId must not be null"))
                .orElseThrow(() -> new RuntimeException("Session not found"));

        SessionExercise exercise = SessionExercise.builder()
                .session(session)
                .title(request.getTitle())
                .instructions(request.getInstructions())
                .requiredFields(request.getRequiredFields())
                .exampleCode(request.getExampleCode())
                .notes(request.getNotes())
                .build();

        sessionExerciseRepository.save(java.util.Objects.requireNonNull(exercise, "exercise must not be null"));
        return toResponse(exercise);
    }

    /**
     * Lấy thông tin bài tập theo ID
     */
    @Override
    public SessionExerciseResponseDTO getById(Long id) {
        SessionExercise exercise = sessionExerciseRepository.findById(java.util.Objects.requireNonNull(id, "id must not be null"))
                .orElseThrow(() -> new RuntimeException("Exercise not found"));
        return toResponse(exercise);
    }

    /**
     * Lấy danh sách bài tập theo session
     */
    @Override
    public List<SessionExerciseResponseDTO> getBySessionId(Long sessionId) {
        return sessionExerciseRepository.findBySession_Id(java.util.Objects.requireNonNull(sessionId, "sessionId must not be null"))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Cập nhật thông tin bài tập
     */
    @Override
    public SessionExerciseResponseDTO update(Long id, SessionExerciseRequestDTO request) {
        SessionExercise exercise = sessionExerciseRepository.findById(java.util.Objects.requireNonNull(id, "id must not be null"))
                .orElseThrow(() -> new RuntimeException("Exercise not found"));

        exercise.setTitle(request.getTitle());
        exercise.setInstructions(request.getInstructions());
        exercise.setRequiredFields(request.getRequiredFields());
        exercise.setExampleCode(request.getExampleCode());
        exercise.setNotes(request.getNotes());

        sessionExerciseRepository.save(java.util.Objects.requireNonNull(exercise, "exercise must not be null"));
        return toResponse(exercise);
    }

    /**
     * Xóa bài tập
     */
    @Override
    public void delete(Long id) {
        if (!sessionExerciseRepository.existsById(java.util.Objects.requireNonNull(id, "id must not be null"))) {
            throw new RuntimeException("Exercise not found");
        }
        sessionExerciseRepository.deleteById(java.util.Objects.requireNonNull(id, "id must not be null"));
    }

    /**
     * Chuyển đổi Entity sang ResponseDTO
     */
    private SessionExerciseResponseDTO toResponse(SessionExercise entity) {
        return SessionExerciseResponseDTO.builder()
                .exerciseId(entity.getId())
                .sessionId(entity.getSession().getId())
                .title(entity.getTitle())
                .instructions(entity.getInstructions())
                .requiredFields(entity.getRequiredFields())
                .exampleCode(entity.getExampleCode())
                .notes(entity.getNotes())
                .build();
    }
}
