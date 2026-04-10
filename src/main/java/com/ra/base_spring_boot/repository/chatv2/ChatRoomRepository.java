package com.ra.base_spring_boot.repository.chatv2;

import com.ra.base_spring_boot.model.chatv2.ChatRoom;
import com.ra.base_spring_boot.model.chatv2.ChatRoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "members", "members.user" })
    Optional<ChatRoom> findById(UUID id);

    @Query("select r from ChatRoom r where r.type = :type")
    List<ChatRoom> findByType(ChatRoomType type);

    @Query("select distinct r from ChatRoom r join fetch r.members m join fetch m.user where r.type = com.ra.base_spring_boot.model.chatv2.ChatRoomType.ONE_TO_ONE and exists (select 1 from ChatRoomMember m1 where m1.room = r and m1.userId = :u1) and exists (select 1 from ChatRoomMember m2 where m2.room = r and m2.userId = :u2)")
    Optional<ChatRoom> findOneToOneRoom(Long u1, Long u2);

    @Query("select distinct r from ChatRoom r join fetch r.members m join fetch m.user where exists (select 1 from ChatRoomMember m2 where m2.room = r and m2.userId = :userId)")
    List<ChatRoom> findByMember(Long userId);
}
