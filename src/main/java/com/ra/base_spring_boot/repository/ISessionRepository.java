package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ISessionRepository extends JpaRepository<Session, Long> {

    List<Session> findByCourse_IdOrderByOrderIndexAsc(Long courseId);

}
