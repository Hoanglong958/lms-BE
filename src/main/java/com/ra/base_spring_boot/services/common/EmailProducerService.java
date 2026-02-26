package com.ra.base_spring_boot.services.common;

import com.ra.base_spring_boot.dto.Gmail.EmailDTO;

public interface EmailProducerService {
    void pushEmailToQueue(EmailDTO emailDTO);
}
