package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.Gmail.EmailDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EmailProducerService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic emailTopic;

    public void pushEmailToQueue(EmailDTO emailDTO) {
        redisTemplate.convertAndSend(
                Objects.requireNonNull(emailTopic.getTopic(), "topic must not be null"),
                Objects.requireNonNull(emailDTO, "emailDTO must not be null")
        );
    }
}
