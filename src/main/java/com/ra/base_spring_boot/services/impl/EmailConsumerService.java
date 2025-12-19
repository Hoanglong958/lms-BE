package com.ra.base_spring_boot.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ra.base_spring_boot.dto.Gmail.EmailDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

// @Slf4j
// @Service
// @RequiredArgsConstructor
// public class EmailConsumerService implements MessageListener {

//     private final GmailService gmailService;
//     private final RedisTemplate<String, Object> redisTemplate;
//     private final ObjectMapper objectMapper = new ObjectMapper();

//     @Override
//     public void onMessage(Message message, byte[] pattern) {
//         try {
//             // Get message body
//             byte[] messageBody = message.getBody();
//             if (messageBody == null) {
//                 log.error("Received empty message");
//                 return;
//             }
            
//             // Deserialize message to EmailDTO
//             String jsonMessage = new String(messageBody);
//             EmailDTO emailDTO = objectMapper.readValue(jsonMessage, EmailDTO.class);
            
//             // Send email using GmailService
//             gmailService.sendEmail(emailDTO);
            
//             log.info("Email sent successfully to: {}", emailDTO.getTo());
//         } catch (Exception e) {
//             log.error("Failed to send email: {}", e.getMessage(), e);
//         }
//     }
// }
