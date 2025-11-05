package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.SessionExercise.SessionExerciseRequestDTO;
import com.ra.base_spring_boot.dto.SessionExercise.SessionExerciseResponseDTO;

import java.util.List;

public interface ISessionExerciseService {

    SessionExerciseResponseDTO create(SessionExerciseRequestDTO request);

    SessionExerciseResponseDTO getById(Long id);

    List<SessionExerciseResponseDTO> getBySessionId(Long sessionId);

    SessionExerciseResponseDTO update(Long id, SessionExerciseRequestDTO request);

    void delete(Long id);
}
