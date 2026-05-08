package com.fitnexus.backend.service;

import com.fitnexus.backend.dto.*;
import com.fitnexus.backend.entity.DietPlan;
import com.fitnexus.backend.entity.WorkoutPlan;
import com.fitnexus.backend.entity.Role;

import java.util.List;

public interface UserManagementService {
    SuperAdminResponse createSuperAdmin(CreateSuperAdminRequest request, Long creatorId);

    SuperAdminResponse getSuperAdmin(Long superAdminId, Long requesterId);

    SuperAdminResponse updateSuperAdmin(Long superAdminId, CreateSuperAdminRequest request, Long updaterId);

    void deleteSuperAdmin(Long superAdminId, Long deleterId);

    List<SuperAdminResponse> getAllSuperAdmins(Long requesterId);

    AdminResponse createAdmin(CreateAdminRequest request, Long superAdminId);

    AdminResponse updateAdmin(Long adminId, CreateAdminRequest request, Long updaterId);

    AdminResponse getAdmin(Long adminId, Long requesterId);

    void deleteAdmin(Long adminId, Long deleterId);

    List<AdminResponse> getAllAdmins(Long requesterId);

    AdminResponse createManager(CreateAdminRequest request, Long creatorId);

    AdminResponse updateManager(Long managerId, CreateAdminRequest request, Long updaterId);

    AdminResponse getManager(Long managerId, Long requesterId);

    void deleteManager(Long managerId, Long deleterId);

    List<AdminResponse> getAllManagers(Long requesterId);

    TrainerResponse createTrainer(CreateTrainerRequest request, Long creatorId);

    TrainerResponse updateTrainer(Long trainerId, CreateTrainerRequest request, Long updaterId);

    TrainerResponse getTrainer(Long trainerId, Long requesterId);

    void deleteTrainer(Long trainerId, Long deleterId);

    List<TrainerResponse> getAllTrainers(Long requesterId);

    CustomerResponse createCustomerSelf(CreateCustomerSelfRequest request);

    CustomerResponse createCustomerByTrainer(CreateCustomerByTrainerRequest request, Long trainerId);

    CustomerResponse updateCustomer(Long customerId, UpdateCustomerDetailsRequest request, Long updaterId);

    CustomerResponse getCustomer(Long customerId, Long requesterId);

    void deleteCustomer(Long customerId, Long deleterId);

    List<CustomerResponse> getAllCustomers(Long requesterId);

    List<CustomerResponse> getCustomersAssignedToTrainer(Long trainerId);

    CustomerResponse activateCustomer(Long customerId, Long trainerId);

    DietPlan assignDietPlanToUser(Long userId, Long dietPlanId);

    DietPlan getMyDietPlan();

    WorkoutPlan assignWorkoutPlanToUser(Long userId, Long workoutPlanId);

    WorkoutPlan getMyWorkoutPlan();

    List<ReportingOptionResponse> getReportingOptions(Role targetRole, Long branchId, Long requesterId);
}

