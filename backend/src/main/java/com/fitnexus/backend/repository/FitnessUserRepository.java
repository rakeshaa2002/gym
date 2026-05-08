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

    @EntityGraph(attributePaths = {"account", "account.createdBy", "account.admin", "account.manager", "account.trainer", "account.assignedDietPlan", "account.assignedWorkoutPlan", "assignedTrainer", "assignedTrainer.account"})
    Optional<FitnessUser> findById(Long id);

    @EntityGraph(attributePaths = {"account", "account.createdBy", "account.admin", "account.manager", "account.trainer", "account.assignedDietPlan", "account.assignedWorkoutPlan", "assignedTrainer", "assignedTrainer.account"})
    List<FitnessUser> findAll();

    @EntityGraph(attributePaths = {"account", "account.createdBy", "account.admin", "account.manager", "account.trainer", "account.assignedDietPlan", "account.assignedWorkoutPlan", "assignedTrainer", "assignedTrainer.account"})
    List<FitnessUser> findByAccount_Admin_Id(Long adminId);

    @EntityGraph(attributePaths = {"account", "account.createdBy", "account.admin", "account.manager", "account.trainer", "account.assignedDietPlan", "account.assignedWorkoutPlan", "assignedTrainer", "assignedTrainer.account"})
    List<FitnessUser> findByAccount_Manager_Id(Long managerId);

    @EntityGraph(attributePaths = {"account", "account.createdBy", "account.admin", "account.manager", "account.trainer", "account.assignedDietPlan", "account.assignedWorkoutPlan", "assignedTrainer", "assignedTrainer.account"})
    List<FitnessUser> findByAccount_Trainer_Id(Long trainerId);

    List<FitnessUser> findByAssignedTrainer(Trainer assignedTrainer);

    List<FitnessUser> findByAssignedTrainer_Id(Long trainerId);

    @EntityGraph(attributePaths = {"account", "account.createdBy", "account.admin", "account.manager", "account.trainer", "account.assignedDietPlan", "account.assignedWorkoutPlan", "assignedTrainer", "assignedTrainer.account"})
    List<FitnessUser> findByAssignedTrainer_Account_Id(Long trainerAccountId);

    @EntityGraph(attributePaths = {"account", "account.createdBy", "account.admin", "account.manager", "account.trainer", "account.assignedDietPlan", "account.assignedWorkoutPlan", "assignedTrainer", "assignedTrainer.account"})
    List<FitnessUser> findByAccount_CreatedBy_Id(Long createdById);

    List<FitnessUser> findByAccount_IsActive(Boolean isActive);

    List<FitnessUser> findByAccount_IsApproved(Boolean isApproved);

    long countByAccount_IsActive(Boolean isActive);

    long countByAssignedTrainer_Id(Long trainerId);

    long countByAssignedTrainer_Account_Id(Long trainerAccountId);
}
