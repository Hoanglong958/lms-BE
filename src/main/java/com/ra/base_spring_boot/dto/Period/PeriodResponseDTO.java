package com.ra.base_spring_boot.dto.Period;

import com.ra.base_spring_boot.model.Period;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeriodResponseDTO {

    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PeriodResponseDTO fromEntity(Period period) {
        return PeriodResponseDTO.builder()
                .id(period.getId())
                .name(period.getName())
                .startTime(period.getStartTime())
                .endTime(period.getEndTime())
                .createdAt(period.getCreatedAt())
                .updatedAt(period.getUpdatedAt())
                .build();
    }
}
