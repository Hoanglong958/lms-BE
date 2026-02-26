package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.resp.NotificationResponseDTO;
import com.ra.base_spring_boot.model.Notification;
import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.model.constants.NotificationType;
import com.ra.base_spring_boot.repository.INotificationRepository;
import com.ra.base_spring_boot.services.IUserNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserNotificationServiceImpl implements IUserNotificationService {

    private final INotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public Page<NotificationResponseDTO> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable)
                .map(NotificationResponseDTO::new);
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    @Override
    public void markAsRead(Long notificationId, Long userId) {
        notificationRepository.markAsReadByNotificationIdAndUserId(notificationId, userId);
    }

    @Override
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Override
    public void sendNotification(User recipient, String title, String message, NotificationType type,
            String referenceUrl) {
        try {
            // 1. Save to database
            Notification notification = Notification.builder()
                    .recipient(recipient)
                    .title(title)
                    .message(message)
                    .type(type)
                    .referenceUrl(referenceUrl)
                    .isRead(false)
                    .build();

            Notification savedNotification = notificationRepository.save(notification);

            // 2. Send real-time update via WebSocket
            NotificationResponseDTO responseDTO = new NotificationResponseDTO(savedNotification);
            String destination = "/topic/notifications/" + recipient.getId();
            messagingTemplate.convertAndSend(destination, responseDTO);

        } catch (Exception e) {
            log.error("Failed to send notification to user {}: {}", recipient.getId(), e.getMessage());
        }
    }
}
