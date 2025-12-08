package com.ra.base_spring_boot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private Upload upload;

    @Data
    public static class Upload {
        private String root;
    }
}

