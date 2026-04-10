package com.ra.base_spring_boot.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AIChatResponse {
    private String answer;
    private String type;
    private boolean success;
    private String errorMessage;

    public static AIChatResponse ok(String answer, String type) {
        return new AIChatResponse(answer, type, true, null);
    }

    public static AIChatResponse error(String errorMessage) {
        return new AIChatResponse(null, null, false, errorMessage);
    }
}
