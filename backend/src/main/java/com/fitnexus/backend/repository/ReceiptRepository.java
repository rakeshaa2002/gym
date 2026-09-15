package com.fitnexus.backend.repository;

import com.fitnexus.backend.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    List<Receipt> findAllByOrderByReceiptDateDesc();

    Optional<Receipt> findByReceiptNumber(String receiptNumber);

    List<Receipt> findByMemberId(Long memberId);

    @Query("SELECT r FROM Receipt r WHERE r.receiptDate BETWEEN :startDate AND :endDate ORDER BY r.receiptDate DESC")
    List<Receipt> findReceiptsByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Receipt r WHERE r.receiptDate BETWEEN :startDate AND :endDate AND r.paymentMethod = :method")
    double sumByPaymentMethodInDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("method") String method);

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Receipt r WHERE r.receiptDate BETWEEN :startDate AND :endDate")
    double sumTotalAmountInDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    List<Receipt> findByPaymentMethod(String paymentMethod);
}
