package com.ra.base_spring_boot.repository.user;

import com.ra.base_spring_boot.model.UserRoadmapProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IUserRoadmapProgressRepository extends JpaRepository<UserRoadmapProgress, Long> {
    Optional<UserRoadmapProgress> findByUserIdAndAssignmentId(Long userId, Long assignmentId);
    List<UserRoadmapProgress> findByUserId(Long userId);
}
