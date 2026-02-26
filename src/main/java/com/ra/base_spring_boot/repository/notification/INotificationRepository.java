package com.ra.base_spring_boot.repository.notification;

import com.ra.base_spring_boot.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface INotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByRecipientIdAndIsReadFalse(Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient.id = :userId")
    void markAllAsReadByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :notificationId AND n.recipient.id = :userId")
    void markAsReadByNotificationIdAndUserId(Long notificationId, Long userId);
}
