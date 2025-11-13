package com.ra.base_spring_boot.model;

import jakarta.persistence.*;
import lombok.Data;


import java.time.Instant;


@Entity
@Table(name = "messages")
@Data
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String roomId;
    private String sender;


    @Column(columnDefinition = "TEXT")
    private String content;
    private Instant createdAt;

    private Long timestamp;
    private String type;
}