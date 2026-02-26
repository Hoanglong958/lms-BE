package com.ra.base_spring_boot.repository.user;

import com.ra.base_spring_boot.model.UserSessionProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IUserSessionProgressRepository extends JpaRepository<UserSessionProgress, Long> {

    Optional<UserSessionProgress> findByUserIdAndSessionId(Long userId, Long sessionId);

    List<UserSessionProgress> findByUserIdAndCourseId(Long userId, Long courseId);
}


