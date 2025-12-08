package com.ra.base_spring_boot.repository.chatv2;

import com.ra.base_spring_boot.model.chatv2.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {
    List<ChatRoomMember> findByRoom_Id(java.util.UUID roomId);
    boolean existsByRoom_IdAndUserId(java.util.UUID roomId, Long userId);
    void deleteByRoom_IdAndUserId(java.util.UUID roomId, Long userId);
}
