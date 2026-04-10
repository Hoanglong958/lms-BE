package com.ra.base_spring_boot.repository.course;

import com.ra.base_spring_boot.model.SessionExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ISessionExerciseRepository extends JpaRepository<SessionExercise, Long> {
    List<SessionExercise> findBySession_Id(Long sessionId);
}
