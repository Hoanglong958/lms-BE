package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.resp.NotificationResponseDTO;
import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.model.constants.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IUserNotificationService {

    Page<NotificationResponseDTO> getUserNotifications(Long userId, Pageable pageable);

    long getUnreadCount(Long userId);

    void markAsRead(Long notificationId, Long userId);

    void markAllAsRead(Long userId);

    void sendNotification(User recipient, String title, String message, NotificationType type, String referenceUrl);
}
