package com.ra.base_spring_boot.services.registration;

import com.ra.base_spring_boot.dto.Registration.RegistrationRequestDTO;
import com.ra.base_spring_boot.dto.Registration.RegistrationResponseDTO;
import com.ra.base_spring_boot.model.User;

import java.util.List;

public interface IRegistrationService {
    RegistrationResponseDTO register(User student, RegistrationRequestDTO dto);

    List<RegistrationResponseDTO> getMyRegistrations(Long studentId);

    List<RegistrationResponseDTO> getAllRegistrations();

    RegistrationResponseDTO confirmPayment(Long registrationId);

    byte[] exportToExcel();

    byte[] generateInvoicePdf(Long registrationId);
}
