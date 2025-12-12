package com.ra.base_spring_boot.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseInit {
    private static final Logger log = LoggerFactory.getLogger(DatabaseInit.class);
    private final JdbcTemplate jdbc;

    @PostConstruct
    public void fixExamParticipants() {
        try {
            jdbc.execute("ALTER TABLE exam_participants DROP COLUMN examRoomId");
            log.info("[DatabaseInit] Dropped column examRoomId from exam_participants");
        } catch (Exception e) {
            log.warn("[DatabaseInit] Skip dropping examRoomId: {}", e.getMessage());
            try {
                jdbc.execute("ALTER TABLE exam_participants MODIFY COLUMN examRoomId BIGINT NULL");
                log.info("[DatabaseInit] Made examRoomId nullable on exam_participants");
            } catch (Exception ex) {
                log.warn("[DatabaseInit] Skip modifying examRoomId: {}", ex.getMessage());
            }
        }
    }
}
