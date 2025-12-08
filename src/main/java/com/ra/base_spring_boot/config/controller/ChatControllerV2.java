package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.dto.chatv2.*;
import com.ra.base_spring_boot.model.chatv2.ChatMessage;
import com.ra.base_spring_boot.model.chatv2.ChatRoom;
import com.ra.base_spring_boot.model.chatv2.ChatRoomMember;
import com.ra.base_spring_boot.services.IChatMessageService;
import com.ra.base_spring_boot.services.IChatService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class ChatControllerV2 {

    private final IChatService chatService;
    private final IChatMessageService chatMessageService;

    // ========== ROOM APIs ==========

    @Operation(summary = "Tạo hoặc lấy room 1-1")
    @PostMapping("/rooms/one-to-one")
    public ResponseEntity<ChatRoom> oneToOne(@RequestBody OneToOneRoomRequest req) {
        return ResponseEntity.ok(chatService.getOrCreateOneToOne(req.getUserId1(), req.getUserId2()));
    }

    @Operation(summary = "Tạo phòng nhóm")
    @PostMapping("/rooms/group")
    public ResponseEntity<ChatRoom> createGroup(@RequestBody GroupCreateRequest req) {
        return ResponseEntity.ok(chatService.createGroup(req));
    }

    @Operation(summary = "Thêm thành viên vào nhóm")
    @PostMapping("/rooms/{roomId}/members")
    public ResponseEntity<Void> addMembers(@PathVariable UUID roomId, @RequestBody AddMembersRequest req,
                                           @RequestParam Long operatorUserId) {
        req.setRoomId(roomId);
        chatService.addMembers(req, operatorUserId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Xóa thành viên khỏi nhóm")
    @DeleteMapping("/rooms/{roomId}/members/{memberId}")
    public ResponseEntity<Void> removeMember(@PathVariable UUID roomId, @PathVariable Long memberId,
                                             @RequestParam Long operatorUserId) {
        chatService.removeMember(roomId, memberId, operatorUserId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Thành viên rời nhóm")
    @PostMapping("/rooms/{roomId}/leave")
    public ResponseEntity<Void> leave(@PathVariable UUID roomId, @RequestParam Long memberId) {
        chatService.leaveRoom(roomId, memberId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Đổi tên phòng")
    @PutMapping("/rooms/{roomId}/name")
    public ResponseEntity<Void> rename(@PathVariable UUID roomId, @RequestBody RenameRequest req,
                                       @RequestParam Long operatorUserId) {
        chatService.renameRoom(roomId, req, operatorUserId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Đổi avatar phòng")
    @PutMapping("/rooms/{roomId}/avatar")
    public ResponseEntity<Void> avatar(@PathVariable UUID roomId, @RequestBody AvatarRequest req,
                                       @RequestParam Long operatorUserId) {
        chatService.updateAvatar(roomId, req.getAvatarUrl(), operatorUserId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Lấy danh sách phòng của user")
    @GetMapping("/rooms/me")
    public ResponseEntity<List<ChatRoom>> myRooms(@RequestParam Long userId) {
        return ResponseEntity.ok(chatService.myRooms(userId));
    }

    @Operation(summary = "Lấy danh sách thành viên của phòng")
    @GetMapping("/rooms/{roomId}/members")
    public ResponseEntity<List<ChatRoomMember>> members(@PathVariable UUID roomId) {
        return ResponseEntity.ok(chatService.roomMembers(roomId));
    }

    @Operation(summary = "Lấy thông tin phòng + last message (tối giản)")
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ChatRoom> getRoom(@PathVariable UUID roomId) {
        return ResponseEntity.ok(chatService.getRoom(roomId));
    }

    // ========== MESSAGE APIs ==========

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
