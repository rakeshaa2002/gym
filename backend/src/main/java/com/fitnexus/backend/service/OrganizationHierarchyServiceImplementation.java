package com.fitnexus.backend.service;

import com.fitnexus.backend.dto.*;
import com.fitnexus.backend.entity.*;
import com.fitnexus.backend.exception.DuplicateResourceException;
import com.fitnexus.backend.exception.EntityNotFoundException;
import com.fitnexus.backend.exception.InvalidOperationException;
import com.fitnexus.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationHierarchyServiceImplementation implements OrganizationHierarchyService {

    private final HeadOfficeRepository headOfficeRepository;
    private final BranchRepository branchRepository;
    private final DepartmentRepository departmentRepository;
    private final TeamRepository teamRepository;
    private final DesignationRepository designationRepository;

    // ============ HEAD OFFICE ============

    @Override
    @Transactional(readOnly = true)
    public List<HeadOfficeResponse> getAllHeadOffices() {
        return headOfficeRepository.findAll().stream().map(this::toHeadOfficeResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public HeadOfficeResponse getHeadOfficeById(Long id) {
        HeadOffice entity = headOfficeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Head office not found with id: " + id));
        return toHeadOfficeResponse(entity);
    }

    @Override
    public HeadOfficeResponse createHeadOffice(HeadOfficeRequest request) {
        String name = requireName(request.getName(), "Head office name is required");

        if (headOfficeRepository.existsByName(name)) {
            throw new DuplicateResourceException("Head office already exists with name: " + name);
        }

        HeadOffice entity = new HeadOffice();
        entity.setName(name);
        entity.setLocation(trimToNull(request.getLocation()));
        entity.setAddress(trimToNull(request.getAddress()));
        entity.setPhone(trimToNull(request.getPhone()));
        entity.setEmail(trimToNull(request.getEmail()));
        entity.setStatus(normalizeStatus(request.getStatus()));

        return toHeadOfficeResponse(headOfficeRepository.save(entity));
    }

    @Override
    public HeadOfficeResponse updateHeadOffice(Long id, HeadOfficeRequest request) {
        HeadOffice entity = headOfficeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Head office not found with id: " + id));

        String name = requireName(request.getName(), "Head office name is required");
        headOfficeRepository.findByName(name).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new DuplicateResourceException("Another head office already uses name: " + name);
            }
        });

        entity.setName(name);
        entity.setLocation(trimToNull(request.getLocation()));
        entity.setAddress(trimToNull(request.getAddress()));
        entity.setPhone(trimToNull(request.getPhone()));
        entity.setEmail(trimToNull(request.getEmail()));
        entity.setStatus(normalizeStatus(request.getStatus()));

        return toHeadOfficeResponse(headOfficeRepository.save(entity));
    }

    @Override
    public void deleteHeadOffice(Long id) {
        HeadOffice entity = headOfficeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Head office not found with id: " + id));

        long branchCount = branchRepository.countByHeadOfficeId(id);
        if (branchCount > 0) {
            throw new InvalidOperationException("Cannot delete head office with linked branches");
        }

        headOfficeRepository.delete(entity);
    }

    // ============ BRANCH ============

    @Override
    @Transactional(readOnly = true)
    public List<BranchResponse> getAllBranches() {
        return branchRepository.findAll().stream().map(this::toBranchResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BranchResponse getBranchById(Long id) {
        Branch entity = branchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Branch not found with id: " + id));
        return toBranchResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchResponse> getBranchesByHeadOffice(Long headOfficeId) {
        return branchRepository.findByHeadOfficeId(headOfficeId).stream().map(this::toBranchResponse).toList();
    }

    @Override
    public BranchResponse createBranch(BranchRequest request) {
        String name = requireName(request.getName(), "Branch name is required");

        headOfficeRepository.findById(request.getHeadOfficeId())
                .orElseThrow(() -> new EntityNotFoundException("Head office not found with id: " + request.getHeadOfficeId()));

        branchRepository.findByName(name).ifPresent(existing -> {
            if (existing.getHeadOfficeId().equals(request.getHeadOfficeId())) {
                throw new DuplicateResourceException("Branch already exists in this head office: " + name);
            }
        });

        Branch entity = new Branch();
        entity.setName(name);
        entity.setHeadOfficeId(request.getHeadOfficeId());
        entity.setLocation(trimToNull(request.getLocation()));
        entity.setAddress(trimToNull(request.getAddress()));
        entity.setPhone(trimToNull(request.getPhone()));
        entity.setEmail(trimToNull(request.getEmail()));
        entity.setManagerName(trimToNull(request.getManagerName()));
        entity.setStatus(normalizeStatus(request.getStatus()));

        return toBranchResponse(branchRepository.save(entity));
    }

    @Override
    public BranchResponse updateBranch(Long id, BranchRequest request) {
        Branch entity = branchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Branch not found with id: " + id));

        String name = requireName(request.getName(), "Branch name is required");

        headOfficeRepository.findById(request.getHeadOfficeId())
                .orElseThrow(() -> new EntityNotFoundException("Head office not found with id: " + request.getHeadOfficeId()));

        branchRepository.findByName(name).ifPresent(existing -> {
            if (!existing.getId().equals(id) && existing.getHeadOfficeId().equals(request.getHeadOfficeId())) {
                throw new DuplicateResourceException("Another branch already exists in this head office: " + name);
            }
        });

        entity.setName(name);
        entity.setHeadOfficeId(request.getHeadOfficeId());
        entity.setLocation(trimToNull(request.getLocation()));
        entity.setAddress(trimToNull(request.getAddress()));
        entity.setPhone(trimToNull(request.getPhone()));
        entity.setEmail(trimToNull(request.getEmail()));
        entity.setManagerName(trimToNull(request.getManagerName()));
        entity.setStatus(normalizeStatus(request.getStatus()));

        return toBranchResponse(branchRepository.save(entity));
    }

    @Override
    public void deleteBranch(Long id) {
        Branch entity = branchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Branch not found with id: " + id));

        long deptCount = departmentRepository.countByBranchId(id);
        if (deptCount > 0) {
            throw new InvalidOperationException("Cannot delete branch with linked departments");
        }

        branchRepository.delete(entity);
    }

    // ============ DEPARTMENT ============

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream().map(this::toDepartmentResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {
        Department entity = departmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Department not found with id: " + id));
        return toDepartmentResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getDepartmentsByBranch(Long branchId) {
        return departmentRepository.findByBranchId(branchId).stream().map(this::toDepartmentResponse).toList();
    }

    @Override
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        String name = requireName(request.getName(), "Department name is required");

        branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new EntityNotFoundException("Branch not found with id: " + request.getBranchId()));

        departmentRepository.findByName(name).ifPresent(existing -> {
            if (existing.getBranchId().equals(request.getBranchId())) {
                throw new DuplicateResourceException("Department already exists in this branch: " + name);
            }
        });

        Department entity = new Department();
        entity.setName(name);
        entity.setBranchId(request.getBranchId());
        entity.setDescription(trimToNull(request.getDescription()));
        entity.setStatus(normalizeStatus(request.getStatus()));

        return toDepartmentResponse(departmentRepository.save(entity));
    }

    @Override
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        Department entity = departmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Department not found with id: " + id));

        String name = requireName(request.getName(), "Department name is required");

        branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new EntityNotFoundException("Branch not found with id: " + request.getBranchId()));

        departmentRepository.findByName(name).ifPresent(existing -> {
            if (!existing.getId().equals(id) && existing.getBranchId().equals(request.getBranchId())) {
                throw new DuplicateResourceException("Another department already exists in this branch: " + name);
            }
        });

        entity.setName(name);
        entity.setBranchId(request.getBranchId());
        entity.setDescription(trimToNull(request.getDescription()));
        entity.setStatus(normalizeStatus(request.getStatus()));

        return toDepartmentResponse(departmentRepository.save(entity));
    }

    @Override
    public void deleteDepartment(Long id) {
        Department entity = departmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Department not found with id: " + id));

        long teamCount = teamRepository.countByDepartmentId(id);
        long designationCount = designationRepository.countByDepartmentId(id);

        if (teamCount > 0 || designationCount > 0) {
            throw new InvalidOperationException("Cannot delete department with linked teams/designations");
        }

        departmentRepository.delete(entity);
    }

    // ============ TEAM ============

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponse> getAllTeams() {
        return teamRepository.findAll().stream().map(this::toTeamResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TeamResponse getTeamById(Long id) {
        Team entity = teamRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Team not found with id: " + id));
        return toTeamResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponse> getTeamsByDepartment(Long departmentId) {
        return teamRepository.findByDepartmentId(departmentId).stream().map(this::toTeamResponse).toList();
    }

    @Override
    public TeamResponse createTeam(TeamRequest request) {
        String name = requireName(request.getName(), "Team name is required");

        departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new EntityNotFoundException("Department not found with id: " + request.getDepartmentId()));

        teamRepository.findByName(name).ifPresent(existing -> {
            if (existing.getDepartmentId().equals(request.getDepartmentId())) {
                throw new DuplicateResourceException("Team already exists in this department: " + name);
            }
        });

        Team entity = new Team();
        entity.setName(name);
        entity.setDepartmentId(request.getDepartmentId());
        entity.setDescription(trimToNull(request.getDescription()));
        entity.setTeamLead(trimToNull(request.getTeamLead()));
        entity.setMemberCount(request.getMemberCount() == null ? 0 : Math.max(request.getMemberCount(), 0));
        entity.setStatus(normalizeStatus(request.getStatus()));

        return toTeamResponse(teamRepository.save(entity));
    }

    @Override
    public TeamResponse updateTeam(Long id, TeamRequest request) {
        Team entity = teamRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Team not found with id: " + id));

        String name = requireName(request.getName(), "Team name is required");

        departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new EntityNotFoundException("Department not found with id: " + request.getDepartmentId()));

        teamRepository.findByName(name).ifPresent(existing -> {
            if (!existing.getId().equals(id) && existing.getDepartmentId().equals(request.getDepartmentId())) {
                throw new DuplicateResourceException("Another team already exists in this department: " + name);
            }
        });

        entity.setName(name);
        entity.setDepartmentId(request.getDepartmentId());
        entity.setDescription(trimToNull(request.getDescription()));
        entity.setTeamLead(trimToNull(request.getTeamLead()));
        entity.setMemberCount(request.getMemberCount() == null ? 0 : Math.max(request.getMemberCount(), 0));
        entity.setStatus(normalizeStatus(request.getStatus()));

        return toTeamResponse(teamRepository.save(entity));
    }

    @Override
    public void deleteTeam(Long id) {
        Team entity = teamRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Team not found with id: " + id));
        teamRepository.delete(entity);
    }

    // ============ DESIGNATION ============

    @Override
    @Transactional(readOnly = true)
    public List<DesignationResponse> getAllDesignations() {
        return designationRepository.findAll().stream().map(this::toDesignationResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DesignationResponse getDesignationById(Long id) {
        Designation entity = designationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Designation not found with id: " + id));
        return toDesignationResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DesignationResponse> getDesignationsByDepartment(Long departmentId) {
        return designationRepository.findByDepartmentId(departmentId).stream().map(this::toDesignationResponse).toList();
    }

    @Override
    public DesignationResponse createDesignation(DesignationRequest request) {
        String name = requireName(request.getName(), "Designation name is required");

        departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new EntityNotFoundException("Department not found with id: " + request.getDepartmentId()));

        designationRepository.findByName(name).ifPresent(existing -> {
            if (existing.getDepartmentId().equals(request.getDepartmentId())) {
                throw new DuplicateResourceException("Designation already exists in this department: " + name);
            }
        });

        Designation entity = new Designation();
        entity.setName(name);
        entity.setDepartmentId(request.getDepartmentId());
        entity.setDescription(trimToNull(request.getDescription()));
        entity.setLevel(trimToNull(request.getLevel()));
        entity.setSalary(request.getSalary() == null ? 0 : Math.max(request.getSalary(), 0));
        entity.setStatus(normalizeStatus(request.getStatus()));

        return toDesignationResponse(designationRepository.save(entity));
    }

    @Override
    public DesignationResponse updateDesignation(Long id, DesignationRequest request) {
        Designation entity = designationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Designation not found with id: " + id));

        String name = requireName(request.getName(), "Designation name is required");

        departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new EntityNotFoundException("Department not found with id: " + request.getDepartmentId()));

        designationRepository.findByName(name).ifPresent(existing -> {
            if (!existing.getId().equals(id) && existing.getDepartmentId().equals(request.getDepartmentId())) {
                throw new DuplicateResourceException("Another designation already exists in this department: " + name);
            }
        });

        entity.setName(name);
        entity.setDepartmentId(request.getDepartmentId());
        entity.setDescription(trimToNull(request.getDescription()));
        entity.setLevel(trimToNull(request.getLevel()));
        entity.setSalary(request.getSalary() == null ? 0 : Math.max(request.getSalary(), 0));
        entity.setStatus(normalizeStatus(request.getStatus()));

        return toDesignationResponse(designationRepository.save(entity));
    }

    @Override
    public void deleteDesignation(Long id) {
        Designation entity = designationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Designation not found with id: " + id));
        designationRepository.delete(entity);
    }

    // ============ CASCADE (EMPLOYEE FORM) ============

    @Override
    @Transactional(readOnly = true)
    public List<BranchResponse> getBranchesForEmployee(Long userId) {
        return getAllBranches();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getDepartmentsForEmployee(Long branchId, Long userId) {
        return getDepartmentsByBranch(branchId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponse> getTeamsForEmployee(Long departmentId, Long userId) {
        return getTeamsByDepartment(departmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DesignationResponse> getDesignationsForEmployee(Long departmentId, Long userId) {
        return getDesignationsByDepartment(departmentId);
    }

    // ============ HELPERS ============

    private String requireName(String value, String message) {
        String normalized = trimToNull(value);
        if (normalized == null) throw new IllegalArgumentException(message);
        return normalized;
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }

    private String normalizeStatus(String status) {
        String normalized = trimToNull(status);
        if (normalized == null) return "ACTIVE";
        String upper = normalized.toUpperCase();
        if (!upper.equals("ACTIVE") && !upper.equals("INACTIVE")) {
            throw new IllegalArgumentException("Status must be ACTIVE or INACTIVE");
        }
        return upper;
    }

    private HeadOfficeResponse toHeadOfficeResponse(HeadOffice e) {
        return new HeadOfficeResponse(
                e.getId(), e.getName(), e.getLocation(), e.getAddress(),
                e.getPhone(), e.getEmail(), e.getStatus(), e.getCreatedAt(), e.getUpdatedAt()
        );
    }

    private BranchResponse toBranchResponse(Branch e) {
        return new BranchResponse(
                e.getId(), e.getName(), e.getHeadOfficeId(), e.getLocation(), e.getAddress(),
                e.getPhone(), e.getEmail(), e.getManagerName(), e.getStatus(), e.getCreatedAt(), e.getUpdatedAt()
        );
    }

    private DepartmentResponse toDepartmentResponse(Department e) {
        return new DepartmentResponse(
                e.getId(), e.getName(), e.getBranchId(), e.getDescription(),
                e.getStatus(), e.getCreatedAt(), e.getUpdatedAt()
        );
    }

    private TeamResponse toTeamResponse(Team e) {
        return new TeamResponse(
                e.getId(), e.getName(), e.getDepartmentId(), e.getDescription(),
                e.getTeamLead(), e.getMemberCount(), e.getStatus(), e.getCreatedAt(), e.getUpdatedAt()
        );
    }

    private DesignationResponse toDesignationResponse(Designation e) {
        return new DesignationResponse(
                e.getId(), e.getName(), e.getDepartmentId(), e.getDescription(),
                e.getLevel(), e.getSalary(), e.getStatus(), e.getCreatedAt(), e.getUpdatedAt()
        );
    }
}
