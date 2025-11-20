package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.ExamParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IExamParticipantRepository extends JpaRepository<ExamParticipant, Long> {
    Optional<ExamParticipant> findByUser_IdAndExamRoomId(Long userId, Long examRoomId);

    List<ExamParticipant> findAllByExamRoomId(Long examRoomId);

}
