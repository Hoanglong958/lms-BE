package com.ra.base_spring_boot.services.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ra.base_spring_boot.dto.chatv2.ChatReadReceipt;
import com.ra.base_spring_boot.dto.chatv2.SendMessageRequest;
import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.model.chatv2.ChatMemberRole;
import com.ra.base_spring_boot.model.chatv2.ChatMessage;
import com.ra.base_spring_boot.model.chatv2.ChatMessageRead;
import com.ra.base_spring_boot.model.chatv2.ChatMessageType;
import com.ra.base_spring_boot.model.chatv2.ChatRoom;
import com.ra.base_spring_boot.model.chatv2.ChatRoomMember;
import com.ra.base_spring_boot.model.chatv2.ChatRoomType;
import com.ra.base_spring_boot.model.constants.NotificationType;
import com.ra.base_spring_boot.repository.chatv2.ChatMessageReadRepository;
import com.ra.base_spring_boot.repository.chatv2.ChatMessageRepository;
import com.ra.base_spring_boot.repository.chatv2.ChatRoomMemberRepository;
import com.ra.base_spring_boot.repository.chatv2.ChatRoomRepository;
import com.ra.base_spring_boot.services.notification.IUserNotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageServiceImpl implements IChatMessageService {

    private final ChatMessageRepository messageRepo;
    private final ChatMessageReadRepository readRepo;
    private final ChatRoomRepository roomRepo;
    private final ChatRoomMemberRepository memberRepo;
    private final IUserNotificationService userNotificationService;
    private final com.ra.base_spring_boot.repository.user.IUserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

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
                .type(req.getType() != null ? req.getType()
        : (req.getFileUrl() != null ? ChatMessageType.FILE : ChatMessageType.TEXT))
                .fileUrl(req.getFileUrl())
                .build();
        ChatMessage saved = messageRepo.save(msg);

        // Send Notifications to other members
        User sender = userRepository.findById(req.getSenderId()).orElse(null);
        String senderName = (sender != null) ? sender.getFullName() : "Ai đó";
        
        List<ChatRoomMember> members = memberRepo.findByRoom_Id(room.getId());
        for (ChatRoomMember member : members) {
            if (!Objects.equals(member.getUserId(), req.getSenderId())) {
                String title = "Tin nhắn mới";
                String content = senderName + ": " + (msg.getContent().length() > 50 ? msg.getContent().substring(0, 47) + "..." : msg.getContent());
                
                if (room.getType() == ChatRoomType.GROUP && room.getName() != null) {
                    title = "Tin nhắn mới từ " + room.getName();
                }

                userNotificationService.sendNotification(
                    member.getUser(),
                    title,
                    content,
                    NotificationType.CHAT,
                    "/chat?room=" + room.getId()
                );
            }
        }
        
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessage> history(UUID roomId, Pageable pageable) {
        Page<ChatMessage> page = messageRepo.findByRoom_IdOrderByCreatedAtDesc(roomId, pageable);
        // Initialize lazy collections to avoid LazyInitializationException during JSON serialization
        page.getContent().forEach(msg -> Hibernate.initialize(msg.getReadReceipts()));
        return page;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessage> attachments(UUID roomId, ChatMessageType type, Pageable pageable) {
        System.out.println("[attachments] query roomId=" + roomId + " type=" + type);
        Page<ChatMessage> page = messageRepo.findByRoom_IdAndTypeOrderByCreatedAtDesc(roomId, type, pageable);
        System.out.println("[attachments] result count=" + page.getNumberOfElements());
        page.getContent().forEach(msg -> {
            System.out.println("[attachments] msg id=" + msg.getId() + " type=" + msg.getType() + " fileUrl=" + msg.getFileUrl());
        });
        page.getContent().forEach(msg -> Hibernate.initialize(msg.getReadReceipts()));
        return page;
    }

    @Override
    public void markRead(UUID messageId, Long userId) {
        Optional<ChatMessageRead> existing = readRepo.findByMessage_IdAndUserId(messageId, userId);
        if (existing.isPresent()) {
            return;
        }
        ChatMessage message = messageRepo.findById(messageId).orElseThrow();
        readRepo.save(ChatMessageRead.builder()
                .message(message)
                .userId(userId)
                .build());
        publishReadReceipt(message.getRoom().getId(), userId, List.of(message.getId()));
    }

    @Override
    public void markReadAll(UUID roomId, Long userId) {
        // Tìm tất cả tin nhắn trong phòng NOT do userId gửi VÀ chưa có record trong ChatMessageRead
        // Để đơn giản và tránh n+1, ta có thể dùng một query JPQL insert hoặc xử lý danh sách ID
        List<ChatMessage> unreadMessages = messageRepo.findUnreadMessagesInRoom(roomId, userId);
        List<UUID> newlyReadIds = new ArrayList<>();
        for (ChatMessage m : unreadMessages) {
            Optional<ChatMessageRead> existing = readRepo.findByMessage_IdAndUserId(m.getId(), userId);
            if (existing.isPresent()) {
                continue;
            }
            ChatMessageRead saved = readRepo.save(ChatMessageRead.builder()
                    .message(m)
                    .userId(userId)
                    .build());
            newlyReadIds.add(saved.getMessage().getId());
        }
        publishReadReceipt(roomId, userId, newlyReadIds);
    }

    @Override
    public void deleteForMe(UUID messageId, Long userId) {
        // Đơn giản: không lưu bảng hide, chỉ bỏ qua (tối thiểu). Thực tế cần bảng message_user_visibility.
        // Để không chặn tiến độ, tạm thời không làm gì.
    }

    @Override
    @Transactional
    public void deleteForAll(UUID messageId, Long operatorUserId) {
        ChatMessage msg = messageRepo.findById(messageId).orElseThrow();
        UUID roomId = msg.getRoom().getId();
        if (!isTeacher(roomId, operatorUserId) && !Objects.equals(operatorUserId, msg.getSenderId())) {
            throw new SecurityException("Only TEACHER or sender can delete for all");
        }
        msg.setDeleted(true);
        ChatMessage saved = messageRepo.save(msg);
        messagingTemplate.convertAndSend("/topic/rooms/" + roomId, saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessage> search(UUID roomId, String keyword, Pageable pageable) {
        Page<ChatMessage> page = messageRepo.findByRoom_IdAndContentContainingIgnoreCaseOrderByCreatedAtDesc(roomId, keyword, pageable);
        page.getContent().forEach(msg -> Hibernate.initialize(msg.getReadReceipts()));
        return page;
    }

    @Override
    @Transactional(readOnly = true)
    public long unreadCount(UUID roomId, Long userId) {
        return messageRepo.countUnreadByRoomAndUser(roomId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long totalUnreadCount(Long userId) {
        return messageRepo.countTotalUnreadByUser(userId);
    }

    private void publishReadReceipt(UUID roomId, Long userId, List<UUID> messageIds) {
        if (roomId == null || userId == null || messageIds == null || messageIds.isEmpty()) {
            return;
        }
        ChatReadReceipt receipt = ChatReadReceipt.builder()
                .roomId(roomId)
                .readerId(userId)
                .messageIds(messageIds)
                .build();
        messagingTemplate.convertAndSend("/topic/rooms/" + roomId + "/read", receipt);
    }
}
