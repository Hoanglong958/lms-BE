package com.ra.base_spring_boot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "sepay")
public class SepayProperties {
    private String webhookSecret;
    private String qrAcc;
    private String qrBank;
}
