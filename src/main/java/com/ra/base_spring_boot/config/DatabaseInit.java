package com.ra.base_spring_boot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DatabaseInit {
    private static final Logger log = LoggerFactory.getLogger(DatabaseInit.class);
    private final JdbcTemplate jdbc;

    @PostConstruct
    public void fixExamParticipants() {
        try {
            // Check if column exists before dropping
            String checkQuery = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_NAME = 'exam_participants' AND COLUMN_NAME = 'examRoomId' " +
                    "AND TABLE_SCHEMA = DATABASE()";
            Integer count = jdbc.queryForObject(checkQuery, Integer.class);
            
            if (count != null && count > 0) {
                jdbc.execute("ALTER TABLE exam_participants DROP COLUMN examRoomId");
                log.info("[DatabaseInit] Successfully dropped column examRoomId from exam_participants");
            } else {
                log.info("[DatabaseInit] Column examRoomId does not exist in exam_participants, no action needed");
            }
        } catch (Exception e) {
            log.warn("[DatabaseInit] Error checking/dropping examRoomId: {}", e.getMessage());
        }
    }
}
