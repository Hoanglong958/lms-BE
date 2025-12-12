package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.chatv2.SendMessageRequest;
import com.ra.base_spring_boot.model.chatv2.*;
import com.ra.base_spring_boot.repository.chatv2.ChatMessageReadRepository;
import com.ra.base_spring_boot.repository.chatv2.ChatMessageRepository;
import com.ra.base_spring_boot.repository.chatv2.ChatRoomMemberRepository;
import com.ra.base_spring_boot.repository.chatv2.ChatRoomRepository;
import com.ra.base_spring_boot.services.IChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageServiceImpl implements IChatMessageService {

    private final ChatMessageRepository messageRepo;
    private final ChatMessageReadRepository readRepo;
    private final ChatRoomRepository roomRepo;
    private final ChatRoomMemberRepository memberRepo;

    private void requireMember(UUID roomId, Long userId) {
        if (!memberRepo.existsByRoom_IdAndUserId(roomId, userId)) {
            throw new SecurityException("User is not a member of this room");
        }
    }

    private boolean isTeacher(UUID roomId, Long userId) {
        return memberRepo.findByRoom_Id(roomId).stream()
                .anyMatch(m -> Objects.equals(m.getUserId(), userId) && m.getRole() == ChatMemberRole.TEACHER);
    }

    @Override
    public ChatMessage send(SendMessageRequest req) {
        requireMember(req.getRoomId(), req.getSenderId());
        ChatRoom room = roomRepo.findById(req.getRoomId()).orElseThrow();
        ChatMessage msg = ChatMessage.builder()
                .room(room)
                .senderId(req.getSenderId())
                .content(req.getContent())
                .type(req.getType() == null ? ChatMessageType.TEXT : req.getType())
                .fileUrl(req.getFileUrl())
                .build();
        return messageRepo.save(msg);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessage> history(UUID roomId, Pageable pageable) {
        return messageRepo.findByRoom_IdOrderByCreatedAtDesc(roomId, pageable);
    }

    @Override
    public void markRead(UUID messageId, Long userId) {
        readRepo.findByMessage_IdAndUserId(messageId, userId)
                .orElseGet(() -> readRepo.save(ChatMessageRead.builder()
                        .message(messageRepo.findById(messageId).orElseThrow())
                        .userId(userId)
                        .build()));
    }

    @Override
    public void markReadAll(UUID roomId, Long userId) {
        // Đơn giản: load 50 tin đầu và đánh dấu
        Page<ChatMessage> page = messageRepo.findByRoom_IdOrderByCreatedAtDesc(roomId, PageRequest.of(0, 50));
        for (ChatMessage m : page.getContent()) {
            markRead(m.getId(), userId);
        }
    }

    @Override
    public void deleteForMe(UUID messageId, Long userId) {
        // Đơn giản: không lưu bảng hide, chỉ bỏ qua (tối thiểu). Thực tế cần bảng message_user_visibility.
        // Để không chặn tiến độ, tạm thời không làm gì.
    }

    @Override
    public void deleteForAll(UUID messageId, Long operatorUserId) {
        ChatMessage msg = messageRepo.findById(messageId).orElseThrow();
        UUID roomId = msg.getRoom().getId();
        if (!isTeacher(roomId, operatorUserId) && !Objects.equals(operatorUserId, msg.getSenderId())) {
            throw new SecurityException("Only TEACHER or sender can delete for all");
        }
        msg.setDeleted(true);
        messageRepo.save(msg);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessage> search(UUID roomId, String keyword, Pageable pageable) {
        return messageRepo.findByRoom_IdAndContentContainingIgnoreCaseOrderByCreatedAtDesc(roomId, keyword, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public long unreadCount(UUID roomId, Long userId) {
        // Đếm đơn giản: tổng số message - số read-by-user (tối thiểu)
        long total = messageRepo.findByRoom_IdOrderByCreatedAtDesc(roomId).size();
        long reads = readRepo.findAll().stream().filter(r -> r.getMessage().getRoom().getId().equals(roomId) && Objects.equals(r.getUserId(), userId)).count();
        return Math.max(0, total - reads);
    }
}
