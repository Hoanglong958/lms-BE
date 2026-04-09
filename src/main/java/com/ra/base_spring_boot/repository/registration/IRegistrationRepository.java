package com.ra.base_spring_boot.repository.registration;

import com.ra.base_spring_boot.model.Registration;
import com.ra.base_spring_boot.model.constants.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;

@Repository
public interface IRegistrationRepository extends JpaRepository<Registration, Long> {

    @Query("SELECT r FROM Registration r JOIN FETCH r.student JOIN FETCH r.course WHERE r.student.id = :studentId ORDER BY r.paymentDate DESC NULLS LAST, r.registrationDate DESC")
    List<Registration> findByStudent_Id(@Param("studentId") Long studentId);


    @Query("SELECT r FROM Registration r JOIN FETCH r.student JOIN FETCH r.course")
    List<Registration> findAll();

    List<Registration> findByStudent_IdAndCourse_Id(Long studentId, Long courseId);

    @Query("""
            SELECT r
            FROM Registration r
            JOIN FETCH r.student
            JOIN FETCH r.course
            WHERE UPPER(r.transferRef) = UPPER(:transferRef)
            ORDER BY r.id DESC
            """)
    List<Registration> findAllByTransferRefIgnoreCase(@Param("transferRef") String transferRef);

    List<Registration> findByCourse_IdAndPaymentStatus(Long courseId, PaymentStatus paymentStatus);

    @Query("SELECT SUM(r.amount) FROM Registration r WHERE r.paymentStatus = :paymentStatus AND r.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByPaymentStatusAndDateBetween(@Param("paymentStatus") PaymentStatus paymentStatus, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT r FROM Registration r JOIN FETCH r.student JOIN FETCH r.course ORDER BY r.paymentDate DESC NULLS LAST, r.registrationDate DESC")
    List<Registration> findRecentTransactions(org.springframework.data.domain.Pageable pageable);
}
