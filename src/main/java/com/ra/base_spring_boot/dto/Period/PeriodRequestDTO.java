package com.ra.base_spring_boot.dto.Period;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
public class PeriodRequestDTO {

    @Schema(example = "Ca sáng")
    private String name;

    @NotNull
    @JsonFormat(pattern = "HH:mm:ss")
    @Schema(type = "string", example = "08:00:00")
    private LocalTime startTime;

    @NotNull
    @JsonFormat(pattern = "HH:mm:ss")
    @Schema(type = "string", example = "10:00:00")
    private LocalTime endTime;
}
