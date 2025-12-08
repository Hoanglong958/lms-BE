package com.ra.base_spring_boot.dto.Gmail;

import java.io.Serializable;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String to;
    private String subject;
    private String templateName;
    private Map<String, Object> templateData;
}
