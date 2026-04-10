package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.resp.NotificationResponseDTO;
import com.ra.base_spring_boot.security.principle.MyUserDetails;
import com.ra.base_spring_boot.services.notification.IUserNotificationService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final IUserNotificationService userNotificationService;

    @GetMapping
    public ResponseEntity<Page<NotificationResponseDTO>> getUserNotifications(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<NotificationResponseDTO> notifications = userNotificationService.getUserNotifications(
                userDetails.getUser().getId(),
                pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal MyUserDetails userDetails) {
        long count = userNotificationService.getUnreadCount(userDetails.getUser().getId());
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal MyUserDetails userDetails) {
        userNotificationService.markAsRead(id, userDetails.getUser().getId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal MyUserDetails userDetails) {
        userNotificationService.markAllAsRead(userDetails.getUser().getId());
        return ResponseEntity.ok().build();
    }
}
