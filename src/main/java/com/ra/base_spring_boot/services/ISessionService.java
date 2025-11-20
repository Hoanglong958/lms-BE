package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.Session.SessionRequestDTO;
import com.ra.base_spring_boot.dto.Session.SessionResponseDTO;
import java.util.List;

public interface ISessionService {
    List<SessionResponseDTO> getByCourse(Long courseId);
    SessionResponseDTO getById(Long id);
    SessionResponseDTO create(SessionRequestDTO dto);
    void delete(Long id);
    SessionResponseDTO update(Long id, SessionRequestDTO dto);
}

