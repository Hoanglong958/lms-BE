package com.ra.base_spring_boot.services.chat;

import com.ra.base_spring_boot.dto.chatv2.AddMembersRequest;
import com.ra.base_spring_boot.dto.chatv2.ChatRoomResponse;
import com.ra.base_spring_boot.dto.chatv2.GroupCreateRequest;
import com.ra.base_spring_boot.dto.chatv2.RenameRequest;
import com.ra.base_spring_boot.model.chatv2.ChatRoomMember;

import java.util.List;
import java.util.UUID;

public interface IChatService {
    ChatRoomResponse getOrCreateOneToOne(Long userId1, Long userId2);
    ChatRoomResponse createGroup(GroupCreateRequest req);
    void addMembers(AddMembersRequest req, Long operatorUserId);
    void removeMember(UUID roomId, Long memberId, Long operatorUserId);
    void leaveRoom(UUID roomId, Long memberId);
    void renameRoom(UUID roomId, RenameRequest req, Long operatorUserId);
    void updateAvatar(UUID roomId, String avatarUrl, Long operatorUserId);
    List<ChatRoomResponse> myRooms(Long userId);
    List<ChatRoomMember> roomMembers(UUID roomId);
    ChatRoomResponse getRoom(UUID roomId);
}
