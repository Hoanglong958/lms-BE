package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.dto.chatv2.AddMembersRequest;
import com.ra.base_spring_boot.dto.chatv2.GroupCreateRequest;
import com.ra.base_spring_boot.dto.chatv2.RenameRequest;
import com.ra.base_spring_boot.dto.chatv2.AvatarRequest;
import com.ra.base_spring_boot.dto.chatv2.OneToOneRoomRequest;
import com.ra.base_spring_boot.model.chatv2.ChatRoom;
import com.ra.base_spring_boot.model.chatv2.ChatRoomMember;
import com.ra.base_spring_boot.services.IChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat/rooms")
@RequiredArgsConstructor
@Tag(name = "29. Chat V2", description = "Quản lý phòng chat 1-1 & nhóm")
public class ChatRoomControllerV2 {

    private final IChatService chatService;

    @Operation(summary = "Tạo hoặc lấy room 1-1")
    @PostMapping("/one-to-one")
    public ResponseEntity<ChatRoom> oneToOne(@RequestBody OneToOneRoomRequest req) {
        return ResponseEntity.ok(chatService.getOrCreateOneToOne(req.getUserId1(), req.getUserId2()));
    }

    @Operation(summary = "Tạo phòng nhóm")
    @PostMapping("/group")
    public ResponseEntity<ChatRoom> createGroup(@RequestBody GroupCreateRequest req) {
        return ResponseEntity.ok(chatService.createGroup(req));
    }

    @Operation(summary = "Thêm thành viên vào nhóm")
    @PostMapping("/{roomId}/members")
    public ResponseEntity<Void> addMembers(@PathVariable UUID roomId, @RequestBody AddMembersRequest req,
                                           @RequestParam Long operatorUserId) {
        req.setRoomId(roomId);
        chatService.addMembers(req, operatorUserId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Xóa thành viên khỏi nhóm")
    @DeleteMapping("/{roomId}/members/{memberId}")
    public ResponseEntity<Void> removeMember(@PathVariable UUID roomId, @PathVariable Long memberId,
                                             @RequestParam Long operatorUserId) {
        chatService.removeMember(roomId, memberId, operatorUserId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Thành viên rời nhóm")
    @PostMapping("/{roomId}/leave")
    public ResponseEntity<Void> leave(@PathVariable UUID roomId, @RequestParam Long memberId) {
        chatService.leaveRoom(roomId, memberId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Đổi tên phòng")
    @PutMapping("/{roomId}/name")
    public ResponseEntity<Void> rename(@PathVariable UUID roomId, @RequestBody RenameRequest req,
                                       @RequestParam Long operatorUserId) {
        chatService.renameRoom(roomId, req, operatorUserId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Đổi avatar phòng")
    @PutMapping("/{roomId}/avatar")
    public ResponseEntity<Void> avatar(@PathVariable UUID roomId, @RequestBody AvatarRequest req,
                                       @RequestParam Long operatorUserId) {
        chatService.updateAvatar(roomId, req.getAvatarUrl(), operatorUserId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Lấy danh sách phòng của user")
    @GetMapping("/me")
    public ResponseEntity<List<ChatRoom>> myRooms(@RequestParam Long userId) {
        return ResponseEntity.ok(chatService.myRooms(userId));
    }

    @Operation(summary = "Lấy danh sách thành viên của phòng")
    @GetMapping("/{roomId}/members")
    public ResponseEntity<List<ChatRoomMember>> members(@PathVariable UUID roomId) {
        return ResponseEntity.ok(chatService.roomMembers(roomId));
    }

    @Operation(summary = "Lấy thông tin phòng (tối giản)")
    @GetMapping("/{roomId}")
    public ResponseEntity<ChatRoom> getRoom(@PathVariable UUID roomId) {
        return ResponseEntity.ok(chatService.getRoom(roomId));
    }
}
