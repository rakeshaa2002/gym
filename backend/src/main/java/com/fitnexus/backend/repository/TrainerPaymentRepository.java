package com.fitnexus.backend.repository;

import com.fitnexus.backend.entity.TrainerPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TrainerPaymentRepository extends JpaRepository<TrainerPayment, Long> {
    List<TrainerPayment> findByTrainerId(Long trainerId);

    List<TrainerPayment> findAllByOrderByPaymentDateDesc();

    @Query("SELECT tp FROM TrainerPayment tp WHERE tp.paymentDate BETWEEN :startDate AND :endDate ORDER BY tp.paymentDate DESC")
    List<TrainerPayment> findPaymentsByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT tp FROM TrainerPayment tp WHERE tp.trainer.id = :trainerId AND tp.paymentDate BETWEEN :startDate AND :endDate ORDER BY tp.paymentDate DESC")
    List<TrainerPayment> findTrainerPaymentsByDateRange(@Param("trainerId") Long trainerId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(tp.totalAmount), 0) FROM TrainerPayment tp WHERE tp.trainer.id = :trainerId")
    double sumTotalPaidToTrainer(@Param("trainerId") Long trainerId);

    @Query("SELECT COALESCE(SUM(tp.totalAmount), 0) FROM TrainerPayment tp WHERE tp.paymentDate BETWEEN :startDate AND :endDate")
    double sumTotalPaymentInDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(tp) FROM TrainerPayment tp WHERE tp.status = :status")
    long countByStatus(@Param("status") String status);
}
