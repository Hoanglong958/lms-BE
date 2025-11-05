package com.ra.base_spring_boot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // FE sẽ kết nối đến endpoint này
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // tạm thời mở cho tất cả domain
                .withSockJS(); // fallback hỗ trợ trình duyệt cũ
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // /topic: cho broadcast, /queue: cho message riêng
        registry.enableSimpleBroker("/topic", "/queue");

        // prefix cho message từ FE gửi lên
        registry.setApplicationDestinationPrefixes("/app");
    }
}
