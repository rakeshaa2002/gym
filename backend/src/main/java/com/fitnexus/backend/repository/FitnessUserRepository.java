package com.fitnexus.backend.repository;

import com.fitnexus.backend.entity.FitnessUser;
import com.fitnexus.backend.entity.Trainer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FitnessUserRepository extends JpaRepository<FitnessUser, Long> {

    @EntityGraph(attributePaths = {"account", "account.createdBy", "assignedTrainer", "assignedTrainer.account"})
    Optional<FitnessUser> findById(Long id);

    @EntityGraph(attributePaths = {"account", "account.createdBy", "assignedTrainer", "assignedTrainer.account"})
    List<FitnessUser> findAll();

    List<FitnessUser> findByAssignedTrainer(Trainer assignedTrainer);

    List<FitnessUser> findByAssignedTrainer_Id(Long trainerId);

    List<FitnessUser> findByAccount_IsActive(Boolean isActive);

    List<FitnessUser> findByAccount_IsApproved(Boolean isApproved);

    long countByAccount_IsActive(Boolean isActive);

    long countByAssignedTrainer_Id(Long trainerId);
}
