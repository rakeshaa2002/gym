package com.fitnexus.backend.repository;

import com.fitnexus.backend.entity.MembershipPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipPlanRepository extends JpaRepository<MembershipPlan, Long> {
    Optional<MembershipPlan> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    List<MembershipPlan> findByActiveTrueOrderByPriceAsc();

    List<MembershipPlan> findAllByOrderByPriceAsc();
}
