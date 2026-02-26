package com.ra.base_spring_boot.dto.resp;

import com.ra.base_spring_boot.model.Notification;
import com.ra.base_spring_boot.model.constants.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponseDTO {
    private Long id;
    private String title;
    private String message;
    private NotificationType type;
    private String referenceUrl;
    private Boolean isRead;
    private LocalDateTime createdAt;

    public NotificationResponseDTO(Notification notification) {
        this.id = notification.getId();
        this.title = notification.getTitle();
        this.message = notification.getMessage();
        this.type = notification.getType();
        this.referenceUrl = notification.getReferenceUrl();
        this.isRead = notification.getIsRead();
        this.createdAt = notification.getCreatedAt();
    }
}
