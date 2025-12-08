package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.dto.chatv2.SendMessageRequest;
import com.ra.base_spring_boot.model.chatv2.ChatMessage;
import com.ra.base_spring_boot.services.IChatMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "29. Chat V2", description = "Gửi, đọc, tra cứu tin nhắn")
public class ChatMessageControllerV2 {

    private final IChatMessageService chatMessageService;

    // ---- MESSAGE APIs ----
    @Operation(summary = "Gửi tin nhắn (REST)")
    @PostMapping("/messages")
    public ResponseEntity<ChatMessage> send(@RequestBody SendMessageRequest req) {
        return ResponseEntity.ok(chatMessageService.send(req));
    }

    @Operation(summary = "Lịch sử tin nhắn (phân trang)")
    @GetMapping("/messages/history")
    public ResponseEntity<Page<ChatMessage>> history(@RequestParam UUID roomId,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "30") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(Math.max(size, 1), 100));
        return ResponseEntity.ok(chatMessageService.history(roomId, pageable));
    }

    @Operation(summary = "Đánh dấu đã đọc (seen)")
    @PostMapping("/messages/{messageId}/read")
    public ResponseEntity<Void> read(@PathVariable UUID messageId, @RequestParam Long userId) {
        chatMessageService.markRead(messageId, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Đánh dấu tất cả đã đọc trong phòng")
    @PostMapping("/rooms/{roomId}/read-all")
    public ResponseEntity<Void> readAll(@PathVariable UUID roomId, @RequestParam Long userId) {
        chatMessageService.markReadAll(roomId, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Xóa tin nhắn (delete for me)")
    @DeleteMapping("/messages/{messageId}/me")
    public ResponseEntity<Void> deleteForMe(@PathVariable UUID messageId, @RequestParam Long userId) {
        chatMessageService.deleteForMe(messageId, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Xóa tin nhắn cho tất cả (teacher or sender)")
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteForAll(@PathVariable UUID messageId, @RequestParam Long operatorUserId) {
        chatMessageService.deleteForAll(messageId, operatorUserId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Đếm tin nhắn chưa đọc")
    @GetMapping("/rooms/{roomId}/unread-count")
    public ResponseEntity<Long> unread(@PathVariable UUID roomId, @RequestParam Long userId) {
        return ResponseEntity.ok(chatMessageService.unreadCount(roomId, userId));
    }

    @Operation(summary = "Tìm kiếm tin nhắn trong phòng")
    @GetMapping("/messages/search")
    public ResponseEntity<Page<ChatMessage>> search(@RequestParam UUID roomId,
                                                    @RequestParam String keyword,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(Math.max(size, 1), 100));
        return ResponseEntity.ok(chatMessageService.search(roomId, keyword, pageable));
    }
}
