package com.ra.base_spring_boot.dto.Registration;

import com.ra.base_spring_boot.model.constants.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationResponseDTO {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long courseId;
    private String courseTitle;
    private BigDecimal amount;
    private PaymentStatus paymentStatus;
    private LocalDateTime registrationDate;
    private LocalDateTime paymentDate;
    private String note;
    private String transferRef;
}
