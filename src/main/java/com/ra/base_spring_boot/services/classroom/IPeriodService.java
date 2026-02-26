package com.ra.base_spring_boot.services.classroom;

import com.ra.base_spring_boot.dto.Period.PeriodRequestDTO;
import com.ra.base_spring_boot.dto.Period.PeriodResponseDTO;

import java.util.List;

public interface IPeriodService {

    PeriodResponseDTO create(PeriodRequestDTO request);

    PeriodResponseDTO update(Long id, PeriodRequestDTO request);

    void delete(Long id);

    List<PeriodResponseDTO> findAll();
}
