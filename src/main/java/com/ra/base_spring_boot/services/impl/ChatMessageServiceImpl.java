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
        Objects.requireNonNull(req, "request must not be null");
        requireMember(Objects.requireNonNull(req.getRoomId(), "roomId must not be null"), Objects.requireNonNull(req.getSenderId(), "senderId must not be null"));
        ChatRoom room = roomRepo.findById(Objects.requireNonNull(req.getRoomId(), "roomId must not be null")).orElseThrow();
        ChatMessage msg = ChatMessage.builder()
                .room(room)
                .senderId(req.getSenderId())
                .content(req.getContent())
                .type(req.getType() == null ? ChatMessageType.TEXT : req.getType())
                .fileUrl(req.getFileUrl())
                .build();
        return messageRepo.save(Objects.requireNonNull(msg, "message must not be null"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessage> history(UUID roomId, Pageable pageable) {
        return messageRepo.findByRoom_IdOrderByCreatedAtDesc(Objects.requireNonNull(roomId, "roomId must not be null"), Objects.requireNonNull(pageable, "pageable must not be null"));
    }

    @Override
    public void markRead(UUID messageId, Long userId) {
        readRepo.findByMessage_IdAndUserId(Objects.requireNonNull(messageId), Objects.requireNonNull(userId))
                .orElseGet(() -> readRepo.save(Objects.requireNonNull(ChatMessageRead.builder()
                        .message(messageRepo.findById(Objects.requireNonNull(messageId)).orElseThrow())
                        .userId(Objects.requireNonNull(userId))
                        .build(), "messageRead must not be null")));
    }

    @Override
    public void markReadAll(UUID roomId, Long userId) {
        // Đơn giản: load 50 tin đầu và đánh dấu
        Page<ChatMessage> page = messageRepo.findByRoom_IdOrderByCreatedAtDesc(Objects.requireNonNull(roomId, "roomId must not be null"), PageRequest.of(0, 50));
        for (ChatMessage m : page.getContent()) {
            markRead(m.getId(), Objects.requireNonNull(userId, "userId must not be null"));
        }
    }

    @Override
    public void deleteForMe(UUID messageId, Long userId) {
        // Đơn giản: không lưu bảng hide, chỉ bỏ qua (tối thiểu). Thực tế cần bảng message_user_visibility.
        // Để không chặn tiến độ, tạm thời không làm gì.
    }

    @Override
    public void deleteForAll(UUID messageId, Long operatorUserId) {
        ChatMessage msg = messageRepo.findById(Objects.requireNonNull(messageId, "messageId must not be null")).orElseThrow();
        UUID roomId = msg.getRoom().getId();
        if (!isTeacher(roomId, Objects.requireNonNull(operatorUserId, "operatorUserId must not be null")) && !Objects.equals(operatorUserId, msg.getSenderId())) {
            throw new SecurityException("Only TEACHER or sender can delete for all");
        }
        msg.setDeleted(true);
        messageRepo.save(Objects.requireNonNull(msg, "message must not be null"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessage> search(UUID roomId, String keyword, Pageable pageable) {
        return messageRepo.findByRoom_IdAndContentContainingIgnoreCaseOrderByCreatedAtDesc(
                Objects.requireNonNull(roomId, "roomId must not be null"),
                Objects.requireNonNull(keyword, "keyword must not be null"),
                Objects.requireNonNull(pageable, "pageable must not be null")
        );
    }

    @Override
    @Transactional(readOnly = true)
    public long unreadCount(UUID roomId, Long userId) {
        // Đếm đơn giản: tổng số message - số read-by-user (tối thiểu)
        UUID safeRoomId = Objects.requireNonNull(roomId, "roomId must not be null");
        Long safeUserId = Objects.requireNonNull(userId, "userId must not be null");
        long total = messageRepo.findByRoom_IdOrderByCreatedAtDesc(safeRoomId).size();
        long reads = readRepo.findAll().stream().filter(r -> r.getMessage().getRoom().getId().equals(safeRoomId) && Objects.equals(r.getUserId(), safeUserId)).count();
        return Math.max(0, total - reads);
    }
}
