package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.Period.PeriodRequestDTO;
import com.ra.base_spring_boot.dto.Period.PeriodResponseDTO;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.Period;
import com.ra.base_spring_boot.repository.IPeriodRepository;
import com.ra.base_spring_boot.services.IPeriodService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PeriodServiceImpl implements IPeriodService {

    private final IPeriodRepository periodRepository;

    @Override
    public PeriodResponseDTO create(PeriodRequestDTO request) {

        if (periodRepository.existsByName(request.getName())) {
            throw new HttpBadRequest("Tên ca học đã tồn tại");
        }

        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new HttpBadRequest("Thời gian bắt đầu phải trước thời gian kết thúc");
        }

        if (periodRepository.existsOverlap(request.getStartTime(), request.getEndTime())) {
            throw new HttpBadRequest("Khoảng thời gian ca học bị trùng với ca khác");
        }

        Period period = Period.builder()
                .name(request.getName())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        periodRepository.save(period);
        return PeriodResponseDTO.fromEntity(period);
    }

    @Override
    public PeriodResponseDTO update(Long id, PeriodRequestDTO request) {
        Period period = periodRepository.findById(id)
                .orElseThrow(() -> new HttpBadRequest("Ca học không tồn tại"));

        if (periodRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new HttpBadRequest("Tên ca học đã tồn tại");
        }

        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new HttpBadRequest("Thời gian bắt đầu phải trước thời gian kết thúc");
        }

        if (periodRepository.existsOverlapExceptId(request.getStartTime(), request.getEndTime(), id)) {
            throw new HttpBadRequest("Khoảng thời gian ca học bị trùng với ca khác");
        }

        period.setName(request.getName());
        period.setStartTime(request.getStartTime());
        period.setEndTime(request.getEndTime());

        periodRepository.save(period);
        return PeriodResponseDTO.fromEntity(period);
    }

    @Override
    public void delete(Long id) {
        if (!periodRepository.existsById(id)) {
            throw new HttpBadRequest("Ca học không tồn tại");
        }
        periodRepository.deleteById(id);
    }

    @Override
    public List<PeriodResponseDTO> findAll() {
        return periodRepository.findAll().stream()
                .map(PeriodResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
