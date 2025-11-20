// ================= IExamParticipantService.java =================
package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.model.ExamParticipant;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IExamParticipantService {

    ExamParticipant joinExam(Long examRoomId, Long userId, LocalDateTime joinTime);
    ExamParticipant submitExam(Long examRoomId, Long userId, LocalDateTime submitTime);
    List<ExamParticipant> getParticipantsByRoom(Long examRoomId);
    ExamParticipant getParticipant(Long userId, Long examRoomId);
    Optional<ExamParticipant> findByUserIdAndExamRoomId(Long userId, Long examRoomId);
}
