package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.chatv2.AddMembersRequest;
import com.ra.base_spring_boot.dto.chatv2.GroupCreateRequest;
import com.ra.base_spring_boot.dto.chatv2.RenameRequest;
import com.ra.base_spring_boot.model.chatv2.*;
import com.ra.base_spring_boot.repository.chatv2.ChatRoomMemberRepository;
import com.ra.base_spring_boot.repository.chatv2.ChatRoomRepository;
import com.ra.base_spring_boot.services.IChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatServiceImpl implements IChatService {

    private final ChatRoomRepository roomRepo;
    private final ChatRoomMemberRepository memberRepo;

    @Override
    public ChatRoom getOrCreateOneToOne(Long userId1, Long userId2) {
        if (Objects.equals(userId1, userId2)) {
            throw new IllegalArgumentException("userId1 must be different from userId2");
        }
        return roomRepo.findOneToOneRoom(userId1, userId2).orElseGet(() -> {
            ChatRoom room = ChatRoom.builder()
                    .type(ChatRoomType.ONE_TO_ONE)
                    .build();
            room = roomRepo.save(room);
            ChatRoom finalRoom = room;
            memberRepo.save(ChatRoomMember.builder().room(finalRoom).userId(userId1).role(ChatMemberRole.USER).build());
            memberRepo.save(ChatRoomMember.builder().room(finalRoom).userId(userId2).role(ChatMemberRole.TEACHER).build());
            return finalRoom;
        });
    }

    @Override
    public ChatRoom createGroup(GroupCreateRequest req) {
        if (req.getCreatedBy() == null) throw new IllegalArgumentException("createdBy is required");
        ChatRoom room = ChatRoom.builder()
                .type(ChatRoomType.GROUP)
                .name(req.getName())
                .avatar(req.getAvatar())
                .createdBy(req.getCreatedBy())
                .build();
        room = roomRepo.save(room);
        Set<Long> ids = new LinkedHashSet<>();
        if (req.getMemberIds()!=null) ids.addAll(req.getMemberIds());
        ids.add(req.getCreatedBy());
        for (Long uid : ids) {
            ChatMemberRole role = Objects.equals(uid, req.getCreatedBy()) ? ChatMemberRole.TEACHER : ChatMemberRole.USER;
            memberRepo.save(ChatRoomMember.builder().room(room).userId(uid).role(role).build());
        }
        return room;
    }

    private void requireTeacher(UUID roomId, Long operatorUserId) {
        List<ChatRoomMember> members = memberRepo.findByRoom_Id(roomId);
        boolean ok = members.stream().anyMatch(m -> Objects.equals(m.getUserId(), operatorUserId) && m.getRole() == ChatMemberRole.TEACHER);
        if (!ok) throw new SecurityException("Only TEACHER can perform this action");
    }

    @Override
    public void addMembers(AddMembersRequest req, Long operatorUserId) {
        requireTeacher(req.getRoomId(), operatorUserId);
        ChatRoom room = roomRepo.findById(req.getRoomId()).orElseThrow();
        for (Long uid : req.getMemberIds()) {
            if (!memberRepo.existsByRoom_IdAndUserId(room.getId(), uid)) {
                memberRepo.save(ChatRoomMember.builder().room(room).userId(uid).role(ChatMemberRole.USER).build());
            }
        }
    }

    @Override
    public void removeMember(UUID roomId, Long memberId, Long operatorUserId) {
        requireTeacher(roomId, operatorUserId);
        memberRepo.deleteByRoom_IdAndUserId(roomId, memberId);
    }

    @Override
    public void leaveRoom(UUID roomId, Long memberId) {
        memberRepo.deleteByRoom_IdAndUserId(roomId, memberId);
    }

    @Override
    public void renameRoom(UUID roomId, RenameRequest req, Long operatorUserId) {
        requireTeacher(roomId, operatorUserId);
        ChatRoom room = roomRepo.findById(roomId).orElseThrow();
        room.setName(req.getName());
        roomRepo.save(room);
    }

    @Override
    public void updateAvatar(UUID roomId, String avatarUrl, Long operatorUserId) {
        requireTeacher(roomId, operatorUserId);
        ChatRoom room = roomRepo.findById(roomId).orElseThrow();
        room.setAvatar(avatarUrl);
        roomRepo.save(room);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatRoom> myRooms(Long userId) {
        return roomRepo.findByMember(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatRoomMember> roomMembers(UUID roomId) {
        return memberRepo.findByRoom_Id(roomId);
    }

    @Override
    @Transactional(readOnly = true)
    public ChatRoom getRoom(UUID roomId) {
        return roomRepo.findById(roomId).orElseThrow();
    }
}
