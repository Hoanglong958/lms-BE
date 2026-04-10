package com.ra.base_spring_boot.dto.Registration;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationRequestDTO {
    private Long courseId;
    private String note;
}
