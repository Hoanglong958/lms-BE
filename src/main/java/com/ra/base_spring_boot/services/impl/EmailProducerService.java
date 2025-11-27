package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.Gmail.EmailDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailProducerService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic emailTopic;

    public void pushEmailToQueue(EmailDTO emailDTO) {
        redisTemplate.convertAndSend(emailTopic.getTopic(), emailDTO);
    }
}
