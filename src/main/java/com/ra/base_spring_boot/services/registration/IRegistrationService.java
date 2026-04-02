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

    RegistrationResponseDTO markPaymentSubmitted(Long registrationId, User student);
    
    RegistrationResponseDTO cancelRegistration(Long registrationId, User student);
    
    List<RegistrationResponseDTO> confirmBulkPayment(List<Long> registrationIds);

    byte[] exportToExcel();

    byte[] generateInvoicePdf(Long registrationId);
}
