package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.chatv2.AddMembersRequest;
import com.ra.base_spring_boot.dto.chatv2.GroupCreateRequest;
import com.ra.base_spring_boot.dto.chatv2.RenameRequest;
import com.ra.base_spring_boot.model.chatv2.ChatRoom;
import com.ra.base_spring_boot.model.chatv2.ChatRoomMember;

import java.util.List;
import java.util.UUID;

public interface IChatService {
    ChatRoom getOrCreateOneToOne(Long userId1, Long userId2);
    ChatRoom createGroup(GroupCreateRequest req);
    void addMembers(AddMembersRequest req, Long operatorUserId);
    void removeMember(UUID roomId, Long memberId, Long operatorUserId);
    void leaveRoom(UUID roomId, Long memberId);
    void renameRoom(UUID roomId, RenameRequest req, Long operatorUserId);
    void updateAvatar(UUID roomId, String avatarUrl, Long operatorUserId);
    List<ChatRoom> myRooms(Long userId);
    List<ChatRoomMember> roomMembers(UUID roomId);
    ChatRoom getRoom(UUID roomId);
}
