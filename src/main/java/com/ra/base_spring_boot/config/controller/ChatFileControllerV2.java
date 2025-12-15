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

import java.util.UUID;
import java.util.Objects;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "28 - Chat V2", description = "Upload file/ảnh cho tin nhắn")
public class ChatFileControllerV2 {

    private final IChatMessageService chatMessageService;
    private final StorageService storageService; // CloudinaryStorageService is @Primary

    @Operation(summary = "Gửi file/ảnh kèm (REST)")
    @PostMapping(path = "/messages/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ChatMessage> sendFile(@RequestParam UUID roomId,
                                                @RequestParam Long senderId,
                                                @RequestPart("file") MultipartFile file) {
        String contentType = Objects.requireNonNullElse(file.getContentType(), "application/octet-stream");
        boolean isImage = contentType.startsWith("image/");
        boolean isVideo = contentType.startsWith("video/");
        String url = isImage ? storageService.storeImage(file) : (isVideo ? storageService.storeVideo(file) : storageService.storeImage(file));
        SendMessageRequest req = new SendMessageRequest();
        req.setRoomId(roomId);
        req.setSenderId(senderId);
        req.setType(isImage ? ChatMessageType.IMAGE : (isVideo ? ChatMessageType.FILE : ChatMessageType.FILE));
        req.setFileUrl(url);
        req.setContent(file.getOriginalFilename());
        return ResponseEntity.ok(chatMessageService.send(req));
    }
}
