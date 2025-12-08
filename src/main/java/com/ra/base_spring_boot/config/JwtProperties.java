package com.ra.base_spring_boot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private Secret secret;
    private Expired expired;

    @Data
    public static class Secret {
        private String key;
    }

    @Data
    public static class Expired {
        private long access;
    }
}

