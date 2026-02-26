package com.ra.base_spring_boot.repository.registration;

import com.ra.base_spring_boot.model.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IRegistrationRepository extends JpaRepository<Registration, Long> {

    @Query("SELECT r FROM Registration r JOIN FETCH r.student JOIN FETCH r.course WHERE r.student.id = :studentId")
    List<Registration> findByStudent_Id(@Param("studentId") Long studentId);

    @Query("SELECT r FROM Registration r JOIN FETCH r.student JOIN FETCH r.course")
    List<Registration> findAll();

    Optional<Registration> findByStudent_IdAndCourse_Id(Long studentId, Long courseId);
}
