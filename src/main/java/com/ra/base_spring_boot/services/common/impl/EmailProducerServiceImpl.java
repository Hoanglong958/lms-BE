package com.ra.base_spring_boot.services.common.impl;

import com.ra.base_spring_boot.dto.Gmail.EmailDTO;
import com.ra.base_spring_boot.services.common.EmailProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.profiles.active", havingValue = "dev", matchIfMissing = false)
public class EmailProducerServiceImpl implements EmailProducerService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic emailTopic;

    @Override
    public void pushEmailToQueue(EmailDTO emailDTO) {
        redisTemplate.convertAndSend(
                Objects.requireNonNull(emailTopic.getTopic(), "topic must not be null"),
                Objects.requireNonNull(emailDTO, "emailDTO must not be null")
        );
    }
}
