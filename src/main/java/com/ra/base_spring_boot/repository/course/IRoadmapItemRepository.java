package com.ra.base_spring_boot.repository.course;

import com.ra.base_spring_boot.model.RoadmapItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IRoadmapItemRepository extends JpaRepository<RoadmapItem, Long> {
}
