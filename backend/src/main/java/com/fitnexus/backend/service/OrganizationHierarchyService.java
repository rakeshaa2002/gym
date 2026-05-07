package com.fitnexus.backend.service;

import com.fitnexus.backend.dto.*;

import java.util.List;

public interface OrganizationHierarchyService {

    // ============ HEAD OFFICE ============
    List<HeadOfficeResponse> getAllHeadOffices();

    HeadOfficeResponse getHeadOfficeById(Long id);

    HeadOfficeResponse createHeadOffice(HeadOfficeRequest request);

    HeadOfficeResponse updateHeadOffice(Long id, HeadOfficeRequest request);

    void deleteHeadOffice(Long id);

    // ============ BRANCH ============
    List<BranchResponse> getAllBranches();

    BranchResponse getBranchById(Long id);

    List<BranchResponse> getBranchesByHeadOffice(Long headOfficeId);

    BranchResponse createBranch(BranchRequest request);

    BranchResponse updateBranch(Long id, BranchRequest request);

    void deleteBranch(Long id);

    // ============ DEPARTMENT ============
    List<DepartmentResponse> getAllDepartments();

    DepartmentResponse getDepartmentById(Long id);

    List<DepartmentResponse> getDepartmentsByBranch(Long branchId);

    DepartmentResponse createDepartment(DepartmentRequest request);

    DepartmentResponse updateDepartment(Long id, DepartmentRequest request);

    void deleteDepartment(Long id);

    // ============ TEAM ============
    List<TeamResponse> getAllTeams();

    TeamResponse getTeamById(Long id);

    List<TeamResponse> getTeamsByDepartment(Long departmentId);

    TeamResponse createTeam(TeamRequest request);

    TeamResponse updateTeam(Long id, TeamRequest request);

    void deleteTeam(Long id);

    // ============ DESIGNATION ============
    List<DesignationResponse> getAllDesignations();

    DesignationResponse getDesignationById(Long id);

    List<DesignationResponse> getDesignationsByDepartment(Long departmentId);

    DesignationResponse createDesignation(DesignationRequest request);

    DesignationResponse updateDesignation(Long id, DesignationRequest request);

    void deleteDesignation(Long id);

    // ============ CASCADE (EMPLOYEE FORM) ============
    List<BranchResponse> getBranchesForEmployee(Long userId);

    List<DepartmentResponse> getDepartmentsForEmployee(Long branchId, Long userId);

    List<TeamResponse> getTeamsForEmployee(Long departmentId, Long userId);

    List<DesignationResponse> getDesignationsForEmployee(Long departmentId, Long userId);
}
