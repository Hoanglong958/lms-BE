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
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatServiceImpl implements IChatService {

    private final ChatRoomRepository roomRepo;
    private final ChatRoomMemberRepository memberRepo;

    @Override
    public ChatRoom getOrCreateOneToOne(Long userId1, Long userId2) {
        Long uid1 = Objects.requireNonNull(userId1, "userId1 must not be null");
        Long uid2 = Objects.requireNonNull(userId2, "userId2 must not be null");
        if (Objects.equals(uid1, uid2)) {
            throw new IllegalArgumentException("userId1 must be different from userId2");
        }
        return roomRepo.findOneToOneRoom(uid1, uid2).orElseGet(() -> {
            ChatRoom room = ChatRoom.builder()
                    .type(ChatRoomType.ONE_TO_ONE)
                    .build();
            room = roomRepo.save(Objects.requireNonNull(room, "room must not be null"));
            ChatRoom finalRoom = room;
            memberRepo.save(Objects.requireNonNull(ChatRoomMember.builder().room(finalRoom).userId(uid1).role(ChatMemberRole.USER).build(), "member must not be null"));
            memberRepo.save(Objects.requireNonNull(ChatRoomMember.builder().room(finalRoom).userId(uid2).role(ChatMemberRole.TEACHER).build(), "member must not be null"));
            return finalRoom;
        });
    }

    @Override
    public ChatRoom createGroup(GroupCreateRequest req) {
        Objects.requireNonNull(req, "request must not be null");
        if (req.getCreatedBy() == null) throw new IllegalArgumentException("createdBy is required");
        ChatRoom room = ChatRoom.builder()
                .type(ChatRoomType.GROUP)
                .name(req.getName())
                .avatar(req.getAvatar())
                .createdBy(req.getCreatedBy())
                .build();
        room = roomRepo.save(Objects.requireNonNull(room, "room must not be null"));
        Set<Long> ids = new LinkedHashSet<>();
        if (req.getMemberIds()!=null) ids.addAll(req.getMemberIds());
        ids.add(req.getCreatedBy());
        for (Long uid : ids) {
            ChatMemberRole role = Objects.equals(uid, req.getCreatedBy()) ? ChatMemberRole.TEACHER : ChatMemberRole.USER;
            memberRepo.save(Objects.requireNonNull(ChatRoomMember.builder().room(room).userId(uid).role(role).build(), "member must not be null"));
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
        Objects.requireNonNull(req, "request must not be null");
        java.util.UUID rid = Objects.requireNonNull(req.getRoomId(), "roomId must not be null");
        requireTeacher(rid, Objects.requireNonNull(operatorUserId, "operatorUserId must not be null"));
        ChatRoom room = roomRepo.findById(rid).orElseThrow();
        for (Long uid : Objects.requireNonNull(req.getMemberIds(), "memberIds must not be null")) {
            if (!memberRepo.existsByRoom_IdAndUserId(room.getId(), uid)) {
                memberRepo.save(Objects.requireNonNull(ChatRoomMember.builder().room(room).userId(uid).role(ChatMemberRole.USER).build(), "member must not be null"));
            }
        }
    }

    @Override
    public void removeMember(UUID roomId, Long memberId, Long operatorUserId) {
        requireTeacher(Objects.requireNonNull(roomId, "roomId must not be null"), Objects.requireNonNull(operatorUserId, "operatorUserId must not be null"));
        memberRepo.deleteByRoom_IdAndUserId(roomId, Objects.requireNonNull(memberId, "memberId must not be null"));
    }

    @Override
    public void leaveRoom(UUID roomId, Long memberId) {
        memberRepo.deleteByRoom_IdAndUserId(Objects.requireNonNull(roomId, "roomId must not be null"), Objects.requireNonNull(memberId, "memberId must not be null"));
    }

    @Override
    public void renameRoom(UUID roomId, RenameRequest req, Long operatorUserId) {
        requireTeacher(Objects.requireNonNull(roomId, "roomId must not be null"), Objects.requireNonNull(operatorUserId, "operatorUserId must not be null"));
        ChatRoom room = roomRepo.findById(roomId).orElseThrow();
        room.setName(req.getName());
        roomRepo.save(Objects.requireNonNull(room, "room must not be null"));
    }

    @Override
    public void updateAvatar(UUID roomId, String avatarUrl, Long operatorUserId) {
        requireTeacher(Objects.requireNonNull(roomId, "roomId must not be null"), Objects.requireNonNull(operatorUserId, "operatorUserId must not be null"));
        ChatRoom room = roomRepo.findById(roomId).orElseThrow();
        room.setAvatar(avatarUrl);
        roomRepo.save(Objects.requireNonNull(room, "room must not be null"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatRoom> myRooms(Long userId) {
        return roomRepo.findByMember(Objects.requireNonNull(userId, "userId must not be null"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatRoomMember> roomMembers(UUID roomId) {
        return memberRepo.findByRoom_Id(Objects.requireNonNull(roomId, "roomId must not be null"));
    }

    @Override
    @Transactional(readOnly = true)
    public ChatRoom getRoom(UUID roomId) {
        return roomRepo.findById(Objects.requireNonNull(roomId, "roomId must not be null")).orElseThrow();
    }
}
