package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.dto.chatv2.SendMessageRequest;
import com.ra.base_spring_boot.model.chatv2.ChatMessage;
import com.ra.base_spring_boot.model.chatv2.ChatMessageType;
import com.ra.base_spring_boot.services.IChatMessageService;
import com.ra.base_spring_boot.services.storage.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.UUID;
import java.util.Objects;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "28 - Chat V2", description = "Upload file/ảnh cho tin nhắn")
public class ChatFileControllerV2 {

    private final IChatMessageService chatMessageService;
    private final StorageService storageService; // CloudinaryStorageService is @Primary
    private final SimpMessagingTemplate messagingTemplate;

    @Operation(summary = "Gửi file/ảnh kèm (REST)")
    @PostMapping(path = "/messages/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ChatMessage> sendFile(@RequestParam UUID roomId,
            @RequestParam Long senderId,
            @RequestPart("file") MultipartFile file) {
        String contentType = Objects.requireNonNullElse(file.getContentType(), "application/octet-stream");
        boolean isImage = contentType.startsWith("image/");
        boolean isVideo = contentType.startsWith("video/");

        String url;
        ChatMessageType type;

        if (isImage) {
            url = storageService.storeImage(file);
            type = ChatMessageType.IMAGE;
        } else if (isVideo) {
            url = storageService.storeVideo(file);
            type = ChatMessageType.FILE; // Or add VIDEO to ChatMessageType if needed, but and existing model uses
                                         // FILE/IMAGE
        } else {
            url = storageService.storeFile(file);
            type = ChatMessageType.FILE;
        }

        SendMessageRequest req = new SendMessageRequest();
        req.setRoomId(roomId);
        req.setSenderId(senderId);
        req.setType(type);
        req.setFileUrl(url);
        req.setContent(file.getOriginalFilename());

        ChatMessage saved = chatMessageService.send(req);
        // Use roomId from request to avoid LazyInitializationException on @JsonIgnore
        // room field
        messagingTemplate.convertAndSend("/topic/rooms/" + req.getRoomId(), saved);
        return ResponseEntity.ok(saved);
    }
}
