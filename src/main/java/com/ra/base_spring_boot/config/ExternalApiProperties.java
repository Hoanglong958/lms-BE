package com.ra.base_spring_boot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "external.api")
public class ExternalApiProperties {
    private String baseUrl;
    private int connectTimeout; // milliseconds
    private int readTimeout;    // milliseconds
}
