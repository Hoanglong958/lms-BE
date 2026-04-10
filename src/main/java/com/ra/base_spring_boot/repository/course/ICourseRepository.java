package com.ra.base_spring_boot.repository.course;

import com.ra.base_spring_boot.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ICourseRepository extends JpaRepository<Course, Long> {

    @Query("SELECT COUNT(c) FROM Course c")
    long countAll();

    @Query("SELECT COUNT(c) FROM Course c WHERE c.createdAt >= :since")
    long countSince(LocalDateTime since);

    @Query("SELECT COUNT(c) FROM Course c WHERE c.createdAt < :before")
    long countBefore(LocalDateTime before);

    @Query("SELECT c FROM Course c WHERE c.createdAt >= :since ORDER BY c.createdAt DESC")
    List<Course> findNewCoursesSince(LocalDateTime since);
    Page<Course> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<Course> findByTitleContainingIgnoreCaseAndIsActive(String title, boolean isActive, Pageable pageable);
    Page<Course> findByIsActive(boolean isActive, Pageable pageable);
    List<Course> findByIsActive(boolean isActive);

    @Query("SELECT c FROM Course c WHERE c.isActive = true AND EXISTS (SELECT 1 FROM ClassCourse cc WHERE cc.course.id = c.id)")
    List<Course> findActiveAndAssigned();

    @Query("SELECT c FROM Course c WHERE c.isActive = true AND EXISTS (SELECT 1 FROM ClassCourse cc WHERE cc.course.id = c.id)")
    Page<Course> findActiveAndAssigned(Pageable pageable);

    @Query("SELECT c FROM Course c WHERE c.isActive = true AND " +
           "EXISTS (SELECT 1 FROM ClassCourse cc WHERE cc.course.id = c.id) AND " +
           "LOWER(c.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<Course> searchActiveAssignedByTitle(@Param("title") String title, Pageable pageable);

    @Query("SELECT c FROM Course c LEFT JOIN Registration r " +
           "ON c.id = r.course.id AND r.student.id = :studentId " +
           "WHERE (:keyword IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (r.paymentStatus IS NULL OR r.paymentStatus != com.ra.base_spring_boot.model.constants.PaymentStatus.CANCELLED) " +
           "AND (:statusStr IS NULL OR " +
           "    (:statusStr = 'NONE' AND r.id IS NULL) OR " +
           "    (:statusStr != 'NONE' AND r.paymentStatus = :statusEnum)) " +
           "AND (:isActive IS NULL OR c.isActive = :isActive) " +
           "AND (:requireAssignment = false OR EXISTS (SELECT 1 FROM ClassCourse cc WHERE cc.course.id = c.id))")
    Page<Course> findWithRegistrationStatus(
            @Param("studentId") Long studentId,
            @Param("keyword") String keyword,
            @Param("statusStr") String statusStr,
            @Param("statusEnum") com.ra.base_spring_boot.model.constants.PaymentStatus statusEnum,
            @Param("isActive") Boolean isActive,
            @Param("requireAssignment") boolean requireAssignment,
            Pageable pageable);
}
