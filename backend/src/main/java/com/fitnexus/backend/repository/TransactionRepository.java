package com.fitnexus.backend.repository;

import com.fitnexus.backend.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findAllByOrderByTransactionDateDesc();

    /**
     * Fetches all transactions with their member and plan eagerly to avoid
     * LazyInitializationException when processing them outside a Hibernate session.
     */
    @Query("SELECT t FROM Transaction t JOIN FETCH t.member JOIN FETCH t.plan")
    List<Transaction> findAllWithPlanAndMember();

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.member.id IN :memberIds AND t.status = 'SUCCESS'")
    double sumAmountByMemberIdIn(@Param("memberIds") java.util.List<Long> memberIds);
}
