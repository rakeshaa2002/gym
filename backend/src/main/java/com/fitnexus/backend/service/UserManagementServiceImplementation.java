package com.fitnexus.backend.service;

import com.fitnexus.backend.dto.*;
import com.fitnexus.backend.entity.*;
import com.fitnexus.backend.exception.InvalidOperationException;
import com.fitnexus.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserManagementServiceImplementation implements UserManagementService {

    private final UserRepository userRepository;
    private final SuperAdminRepository superAdminRepository;
    private final AdminRepository adminRepository;
    private final ManagerRepository managerRepository;
    private final TrainerRepository trainerRepository;
    private final CounselorRepository counselorRepository;
    private final FitnessUserRepository fitnessUserRepository;
    private final DietPlanRepository dietPlanRepository;
    private final WorkoutPlanRepository workoutPlanRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    // Member-owned records that must be cleared before a member can be deleted
    // (their FKs would otherwise block the delete).
    private final AttendanceRepository attendanceRepository;
    private final GoalRepository goalRepository;
    private final ProgressEntryRepository progressEntryRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final NotificationRepository notificationRepository;
    private final UserWorkoutScheduleRepository userWorkoutScheduleRepository;
    private final MembershipRequestRepository membershipRequestRepository;
    private final TransactionRepository transactionRepository;

    private int getRoleLevel(Role role) {
        switch (role) {
            case SUPER_ADMIN:
                return 5;
            case ADMIN:
                return 4;
            case MANAGER:
                return 3;
            case TRAINER:
            case COUNSELOR:
                return 2;
            case USER:
                return 1;
            default:
                return 0;
        }
    }

    private Users getAccount(Long userId, String label) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new SecurityException(label + " not found"));
    }

    private Users getAuthenticatedAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("You are not authenticated");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new SecurityException("Authenticated user not found"));
    }

    private Users getRequesterOrThrow(Long requesterId) {
        Users requester = getAuthenticatedAccount();
        if (requesterId != null && !requesterId.equals(requester.getId())) {
            throw new SecurityException("Requester does not match the authenticated user");
        }
        return requester;
    }

    private void applyHierarchyLinks(Users account, Users creator) {
        if (account == null || creator == null) {
            return;
        }

        switch (account.getRole()) {
            case ADMIN:
                account.setAdmin(account);
                break;
            case MANAGER:
                account.setAdmin(creator.getAdmin() != null ? creator.getAdmin() : (creator.getRole() == Role.ADMIN ? creator : null));
                account.setManager(account);
                break;
            case TRAINER:
            case COUNSELOR:
                account.setAdmin(creator.getAdmin() != null ? creator.getAdmin() : (creator.getRole() == Role.ADMIN ? creator : null));
                account.setManager(creator.getManager() != null ? creator.getManager() : (creator.getRole() == Role.MANAGER ? creator : null));
                account.setTrainer(account);
                break;
            case USER:
                account.setAdmin(creator.getAdmin() != null ? creator.getAdmin() : (creator.getRole() == Role.ADMIN ? creator : null));
                account.setManager(creator.getManager() != null ? creator.getManager() : (creator.getRole() == Role.MANAGER ? creator : null));
                account.setTrainer(creator.getTrainer() != null ? creator.getTrainer() : (creator.getRole() == Role.TRAINER ? creator : null));
                break;
            default:
                break;
        }
    }


    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String buildDisplayName(String firstName, String lastName) {
        String first = normalizeOptional(firstName);
        String last = normalizeOptional(lastName);
        if (first == null) {
            return last == null ? "" : last;
        }
        return last == null ? first : first + " " + last;
    }

    private String toDateString(LocalDate value) {
        return value == null ? null : value.toString();
    }
    private Users createAccount(String email, String password, String firstName, String lastName, Role role, Users creator) {
        Users account = new Users();
        account.setEmail(email);
        account.setPassword(passwordEncoder.encode(password));
        account.setName(buildDisplayName(firstName, lastName));
        account.setRole(role);
        account.setCreatedBy(creator);
        account.setIsActive(true);
        account.setIsApproved(true);
        applyHierarchyLinks(account, creator);
        return account;
    }

    private void checkCreatePermission(Long creatorId, Role roleToCreate) {
        Users creator = getAccount(creatorId, "Creator");
        if (getRoleLevel(roleToCreate) >= getRoleLevel(creator.getRole())) {
            throw new SecurityException("Cannot create users at your level or above. Hierarchy: SUPER_ADMIN > ADMIN > MANAGER > TRAINER > USER");
        }
    }

    private void checkUpdatePermission(Long updaterId, Long targetUserId) {
        if (updaterId.equals(targetUserId)) {
            return;
        }
        Users updater = getAccount(updaterId, "Updater");
        Users target = getAccount(targetUserId, "Target user");
        if (getRoleLevel(target.getRole()) >= getRoleLevel(updater.getRole())) {
            throw new SecurityException("Cannot update users at your level or above");
        }
    }

    private void checkDeletePermission(Long deleterId, Long targetUserId) {
        if (deleterId.equals(targetUserId)) {
            throw new SecurityException("Cannot delete your own account");
        }
        checkUpdatePermission(deleterId, targetUserId);
    }

    private void checkViewPermission(Long requesterId, Long targetUserId) {
        if (requesterId.equals(targetUserId)) {
            return;
        }
        Users requester = getAccount(requesterId, "Requester");
        Users target = getAccount(targetUserId, "Target user");
        if (getRoleLevel(target.getRole()) >= getRoleLevel(requester.getRole())) {
            throw new SecurityException("Cannot view users at your level or above");
        }
    }

    private void ensureRequesterCanList(Long requesterId, Role listedRole, String label) {
        Users requester = getAccount(requesterId, "Requester");
        if (getRoleLevel(listedRole) >= getRoleLevel(requester.getRole())) {
            throw new SecurityException("You don't have permission to view " + label);
        }
    }

    @Override
    public SuperAdminResponse createSuperAdmin(CreateSuperAdminRequest request, Long creatorId) {
        checkCreatePermission(creatorId, Role.SUPER_ADMIN);
        Users creator = getAccount(creatorId, "Creator");
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        SuperAdmin superAdmin = new SuperAdmin();
        superAdmin.setAccount(createAccount(request.getEmail(), request.getPassword(), request.getFirstName(), request.getLastName(), Role.SUPER_ADMIN, creator));
        superAdmin.setFirstName(request.getFirstName());
        superAdmin.setLastName(normalizeOptional(request.getLastName()));
        return mapSuperAdminToResponse(superAdminRepository.save(superAdmin));
    }

    @Override
    public SuperAdminResponse getSuperAdmin(Long superAdminId, Long requesterId) {
        checkViewPermission(requesterId, superAdminId);
        SuperAdmin superAdmin = superAdminRepository.findById(superAdminId)
                .orElseThrow(() -> new SecurityException("SuperAdmin not found"));
        return mapSuperAdminToResponse(superAdmin);
    }

    @Override
    public SuperAdminResponse updateSuperAdmin(Long superAdminId, CreateSuperAdminRequest request, Long updaterId) {
        checkUpdatePermission(updaterId, superAdminId);
        SuperAdmin superAdmin = superAdminRepository.findById(superAdminId)
                .orElseThrow(() -> new SecurityException("SuperAdmin not found"));
        Users account = superAdmin.getAccount();
        account.setEmail(request.getEmail());
        if (!Boolean.TRUE.equals(request.getKeepPassword())) {
            account.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        account.setName(buildDisplayName(request.getFirstName(), request.getLastName()));
        superAdmin.setFirstName(request.getFirstName());
        superAdmin.setLastName(normalizeOptional(request.getLastName()));
        return mapSuperAdminToResponse(superAdminRepository.save(superAdmin));
    }

    @Override
    public void deleteSuperAdmin(Long superAdminId, Long deleterId) {
        checkDeletePermission(deleterId, superAdminId);
        SuperAdmin superAdmin = superAdminRepository.findById(superAdminId)
                .orElseThrow(() -> new SecurityException("SuperAdmin not found"));
        superAdminRepository.delete(superAdmin);
    }

    @Override
    public List<SuperAdminResponse> getAllSuperAdmins(Long requesterId) {
        ensureRequesterCanList(requesterId, Role.SUPER_ADMIN, "super admins");
        return superAdminRepository.findAll().stream()
                .map(this::mapSuperAdminToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AdminResponse createAdmin(CreateAdminRequest request, Long creatorId) {
        checkCreatePermission(creatorId, Role.ADMIN);
        Users creator = getAccount(creatorId, "Creator");
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        Admin admin = new Admin();
        admin.setAccount(createAccount(request.getEmail(), request.getPassword(), request.getFirstName(), request.getLastName(), Role.ADMIN, creator));
        copyAdminFields(admin, request);
        return mapAdminToResponse(adminRepository.save(admin));
    }

    @Override
    public AdminResponse getAdmin(Long adminId, Long requesterId) {
        checkViewPermission(requesterId, adminId);
        return mapAdminToResponse(adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found")));
    }

    @Override
    public AdminResponse updateAdmin(Long adminId, CreateAdminRequest request, Long updaterId) {
        checkUpdatePermission(updaterId, adminId);
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        admin.getAccount().setName(buildDisplayName(request.getFirstName(), request.getLastName()));
        if (!Boolean.TRUE.equals(request.getKeepPassword())) {
            admin.getAccount().setPassword(passwordEncoder.encode(request.getPassword()));
        }
        copyAdminFields(admin, request);
        return mapAdminToResponse(adminRepository.save(admin));
    }

    @Override
    public void deleteAdmin(Long adminId, Long deleterId) {
        checkDeletePermission(deleterId, adminId);
        adminRepository.delete(adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found")));
    }

    @Override
    public List<AdminResponse> getAllAdmins(Long requesterId) {
        ensureRequesterCanList(requesterId, Role.ADMIN, "admins");
        return adminRepository.findAll().stream()
                .map(this::mapAdminToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AdminResponse createManager(CreateAdminRequest request, Long creatorId) {
        checkCreatePermission(creatorId, Role.MANAGER);
        Users creator = getAccount(creatorId, "Creator");
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        Manager manager = new Manager();
        manager.setAccount(createAccount(request.getEmail(), request.getPassword(), request.getFirstName(), request.getLastName(), Role.MANAGER, creator));
        copyManagerFields(manager, request);
        return mapManagerToResponse(managerRepository.save(manager));
    }

    @Override
    public AdminResponse getManager(Long managerId, Long requesterId) {
        checkViewPermission(requesterId, managerId);
        return mapManagerToResponse(managerRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found")));
    }

    @Override
    public AdminResponse updateManager(Long managerId, CreateAdminRequest request, Long updaterId) {
        checkUpdatePermission(updaterId, managerId);
        Manager manager = managerRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));
        manager.getAccount().setName(buildDisplayName(request.getFirstName(), request.getLastName()));
        if (!Boolean.TRUE.equals(request.getKeepPassword())) {
            manager.getAccount().setPassword(passwordEncoder.encode(request.getPassword()));
        }
        copyManagerFields(manager, request);
        return mapManagerToResponse(managerRepository.save(manager));
    }

    @Override
    public void deleteManager(Long managerId, Long deleterId) {
        checkDeletePermission(deleterId, managerId);
        managerRepository.delete(managerRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found")));
    }

    @Override
    public List<AdminResponse> getAllManagers(Long requesterId) {
        ensureRequesterCanList(requesterId, Role.MANAGER, "managers");
        return managerRepository.findAll().stream()
                .map(this::mapManagerToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TrainerResponse createTrainer(CreateTrainerRequest request, Long creatorId) {
        checkCreatePermission(creatorId, Role.TRAINER);
        Users creator = getAccount(creatorId, "Creator");
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        Trainer trainer = new Trainer();
        trainer.setAccount(createAccount(request.getEmail(), request.getPassword(), request.getFirstName(), request.getLastName(), Role.TRAINER, creator));
        copyTrainerFields(trainer, request);
        return mapTrainerToResponse(trainerRepository.save(trainer));
    }

    @Override
    public TrainerResponse getTrainer(Long trainerId, Long requesterId) {
        checkViewPermission(requesterId, trainerId);
        return mapTrainerToResponse(trainerRepository.findById(trainerId)
                .orElseThrow(() -> new RuntimeException("Trainer not found")));
    }

    @Override
    public TrainerResponse updateTrainer(Long trainerId, CreateTrainerRequest request, Long updaterId) {
        checkUpdatePermission(updaterId, trainerId);
        Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new RuntimeException("Trainer not found"));
        trainer.getAccount().setName(buildDisplayName(request.getFirstName(), request.getLastName()));
        if (!Boolean.TRUE.equals(request.getKeepPassword())) {
            trainer.getAccount().setPassword(passwordEncoder.encode(request.getPassword()));
        }
        copyTrainerFields(trainer, request);
        return mapTrainerToResponse(trainerRepository.save(trainer));
    }

    @Override
    public void deleteTrainer(Long trainerId, Long deleterId) {
        checkDeletePermission(deleterId, trainerId);
        trainerRepository.delete(trainerRepository.findById(trainerId)
                .orElseThrow(() -> new RuntimeException("Trainer not found")));
    }

    @Override
    public List<TrainerResponse> getAllTrainers(Long requesterId) {
        getRequesterOrThrow(requesterId);
        return trainerRepository.findAll().stream()
                .map(this::mapTrainerToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CounselorResponse createCounselor(CreateCounselorRequest request, Long creatorId) {
        checkCreatePermission(creatorId, Role.COUNSELOR);
        Users creator = getAccount(creatorId, "Creator");
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        Counselor counselor = new Counselor();
        counselor.setAccount(createAccount(request.getEmail(), request.getPassword(), request.getFirstName(), request.getLastName(), Role.COUNSELOR, creator));
        counselor.setFirstName(request.getFirstName());
        counselor.setLastName(normalizeOptional(request.getLastName()));
        counselor.setPhone(request.getPhone());
        return mapCounselorToResponse(counselorRepository.save(counselor));
    }

    @Override
    public CounselorResponse getCounselor(Long counselorId, Long requesterId) {
        checkViewPermission(requesterId, counselorId);
        return mapCounselorToResponse(counselorRepository.findById(counselorId)
                .orElseThrow(() -> new RuntimeException("Counselor not found")));
    }

    @Override
    public CounselorResponse updateCounselor(Long counselorId, CreateCounselorRequest request, Long updaterId) {
        checkUpdatePermission(updaterId, counselorId);
        Counselor counselor = counselorRepository.findById(counselorId)
                .orElseThrow(() -> new RuntimeException("Counselor not found"));
        counselor.getAccount().setName(buildDisplayName(request.getFirstName(), request.getLastName()));
        if (!Boolean.TRUE.equals(request.getKeepPassword())) {
            counselor.getAccount().setPassword(passwordEncoder.encode(request.getPassword()));
        }
        counselor.setFirstName(request.getFirstName());
        counselor.setLastName(normalizeOptional(request.getLastName()));
        counselor.setPhone(request.getPhone());
        return mapCounselorToResponse(counselorRepository.save(counselor));
    }

    @Override
    public void deleteCounselor(Long counselorId, Long deleterId) {
        checkDeletePermission(deleterId, counselorId);
        counselorRepository.delete(counselorRepository.findById(counselorId)
                .orElseThrow(() -> new RuntimeException("Counselor not found")));
    }

    @Override
    public List<CounselorResponse> getAllCounselors(Long requesterId) {
        ensureRequesterCanList(requesterId, Role.COUNSELOR, "counselors");
        return counselorRepository.findAll().stream()
                .map(this::mapCounselorToResponse)
                .collect(Collectors.toList());
    }

    private CounselorResponse mapCounselorToResponse(Counselor counselor) {
        CounselorResponse response = new CounselorResponse();
        response.setId(counselor.getId());
        response.setEmail(counselor.getAccount().getEmail());
        response.setFirstName(counselor.getFirstName());
        response.setLastName(counselor.getLastName());
        response.setPhone(counselor.getPhone());
        response.setIsActive(counselor.getAccount().getIsActive());
        response.setJoinDate(counselor.getJoinDate());
        response.setCreatedAt(counselor.getAccount().getCreatedAt());
        response.setUpdatedAt(counselor.getAccount().getUpdatedAt());
        return response;
    }




    @Override
    public List<ReportingOptionResponse> getReportingOptions(Role targetRole, Long branchId, Long requesterId) {
        Users requester = getAccount(requesterId, "Requester");
        if (getRoleLevel(targetRole) >= getRoleLevel(requester.getRole())) {
            throw new SecurityException("You can only assign reporting users for roles below your level");
        }
        return userRepository.findReportingOptions(branchId, getAllowedReportsToRoles(targetRole)).stream()
                .filter(user -> !user.getId().equals(requesterId))
                .map(user -> new ReportingOptionResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getBranchId()))
                .collect(Collectors.toList());
    }

    private List<Role> getAllowedReportsToRoles(Role targetRole) {
        switch (targetRole) {
            case TRAINER:
                return List.of(Role.MANAGER, Role.ADMIN);
            case MANAGER:
                return List.of(Role.ADMIN);
            case ADMIN:
                return List.of(Role.SUPER_ADMIN);
            default:
                return List.of();
        }
    }

    private void applyReportsTo(Users account, Long reportsToId) {
        if (reportsToId == null) {
            account.setReportsTo(null);
            return;
        }
        Users reportsTo = getAccount(reportsToId, "Reports To user");
        if (!getAllowedReportsToRoles(account.getRole()).contains(reportsTo.getRole())) {
            throw new IllegalArgumentException(account.getRole() + " cannot report to " + reportsTo.getRole());
        }
        if (account.getBranchId() != null && reportsTo.getRole() != Role.SUPER_ADMIN && reportsTo.getBranchId() != null && !account.getBranchId().equals(reportsTo.getBranchId())) {
            throw new IllegalArgumentException("Reports To user must belong to the same branch");
        }
        account.setReportsTo(reportsTo);
    }

    @Override
    public CustomerResponse createCustomerSelf(CreateCustomerSelfRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        Users account = createAccount(request.getEmail(), request.getPassword(), request.getFirstName(), request.getLastName(), Role.USER, null);
        account.setIsActive(false);
        account.setIsApproved(false);

        FitnessUser customer = new FitnessUser();
        customer.setAccount(account);
        customer.setFirstName(request.getFirstName());
        customer.setLastName(normalizeOptional(request.getLastName()));
        customer.setDetailsCompleted(false);
        return mapCustomerToResponse(fitnessUserRepository.save(customer));
    }

    @Override
    public CustomerResponse createCustomerByTrainer(CreateCustomerByTrainerRequest request, Long trainerId) {
        checkCreatePermission(trainerId, Role.USER);
        Users creator = getAccount(trainerId, "Creator");
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        FitnessUser customer = new FitnessUser();
        customer.setAccount(createAccount(request.getEmail(), request.getPassword(), request.getFirstName(), request.getLastName(), Role.USER, creator));
        customer.setFirstName(request.getFirstName());
        customer.setLastName(normalizeOptional(request.getLastName()));
        copyCustomerFields(customer, request);
        applyOrgFields(customer.getAccount(),
                creator.getHeadOfficeId(),
                creator.getBranchId(),
                creator.getDepartmentId(),
                creator.getTeamId(),
                creator.getDesignationId());
        Long assignedTrainerAccountId = request.getAssignedTrainerId();
        if (assignedTrainerAccountId == null) {
            throw new RuntimeException("Assigned trainer is required");
        }
        Users trainerAccount = getAccount(assignedTrainerAccountId, "Trainer");
        if (trainerAccount.getRole() != Role.TRAINER) {
            throw new RuntimeException("Selected user is not a trainer");
        }
        Trainer trainer = trainerRepository.findByAccount_Id(assignedTrainerAccountId)
                .orElseThrow(() -> new RuntimeException("Trainer profile not found"));
        customer.setAssignedTrainer(trainer);
        customer.getAccount().setTrainer(trainerAccount);
        customer.getAccount().setManager(trainerAccount.getManager());
        customer.setDetailsCompleted(true);
        return mapCustomerToResponse(fitnessUserRepository.save(customer));
    }

    @Override
    public CustomerResponse updateCustomer(Long customerId, UpdateCustomerDetailsRequest request, Long updaterId) {
        Users updater = getAccount(updaterId, "Updater");
        if (updater.getRole() == Role.USER && !updaterId.equals(customerId)) {
            throw new SecurityException("Users can only update their own details");
        }
        if (updater.getRole() != Role.USER) {
            checkUpdatePermission(updaterId, customerId);
        }
        FitnessUser customer = fitnessUserRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        Users account = customer.getAccount();
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            account.setEmail(request.getEmail().trim());
        }
        String updatedName = buildDisplayName(
                request.getFirstName() != null ? request.getFirstName() : customer.getFirstName(),
                request.getLastName() != null ? request.getLastName() : customer.getLastName()
        );
        account.setName(updatedName);
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            account.setPassword(passwordEncoder.encode(request.getPassword().trim()));
        }
        applyOrgFields(account, request.getHeadOfficeId(), request.getBranchId(), request.getDepartmentId(), request.getTeamId(), request.getDesignationId());
        Long assignedTrainerAccountId = request.getAssignedTrainerId();
        if (assignedTrainerAccountId == null) {
            throw new RuntimeException("Assigned trainer is required");
        }
        Users trainerAccount = getAccount(assignedTrainerAccountId, "Trainer");
        if (trainerAccount.getRole() != Role.TRAINER) {
            throw new RuntimeException("Selected user is not a trainer");
        }
        Trainer trainer = trainerRepository.findByAccount_Id(assignedTrainerAccountId)
                .orElseThrow(() -> new RuntimeException("Trainer profile not found"));
        customer.setAssignedTrainer(trainer);
        account.setTrainer(trainerAccount);
        account.setManager(trainerAccount.getManager());
        copyCustomerFields(customer, request);
        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            customer.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null) {
            customer.setLastName(normalizeOptional(request.getLastName()));
        }
        customer.setDetailsCompleted(true);
        CustomerResponse saved = mapCustomerToResponse(fitnessUserRepository.save(customer));
        // Notify the member when an admin (not the member themselves) edits their details.
        if (!updaterId.equals(customerId)) {
            notificationService.createForUser(account, "INFO", "Profile updated",
                    "Your details were updated by " + updater.getName(), "/profile");
        }
        return saved;
    }

    @Override
    public CustomerResponse getCustomer(Long customerId, Long requesterId) {
        checkViewPermission(requesterId, customerId);
        return mapCustomerToResponse(fitnessUserRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found")));
    }

    @Override
    public void deleteCustomer(Long customerId, Long deleterId) {
        checkDeletePermission(deleterId, customerId);
        FitnessUser customer = fitnessUserRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Clear member-owned records first; their foreign keys would otherwise
        // block the delete (the cause of the previous "could not execute statement").
        attendanceRepository.deleteByUserId(customerId);
        goalRepository.deleteByUserId(customerId);
        progressEntryRepository.deleteByUserId(customerId);
        chatMessageRepository.deleteByMemberId(customerId);
        notificationRepository.deleteByRecipientId(customerId);
        userWorkoutScheduleRepository.deleteByUserId(customerId);
        membershipRequestRepository.deleteByMemberId(customerId);

        fitnessUserRepository.delete(customer);
    }

    @Override
    public List<CustomerResponse> getAllCustomers(Long requesterId) {
        Users requester = getRequesterOrThrow(requesterId);
        return getVisibleCustomersForRequester(requester).stream()
                .map(this::mapCustomerToResponse)
                .collect(Collectors.toList());
    }

     @Override
     public List<CustomerResponse> getCustomersAssignedToTrainer(Long trainerId) {
         Users requester = getAuthenticatedAccount();
         if (requester.getRole() == Role.TRAINER && !requester.getId().equals(trainerId)) {
             throw new SecurityException("Trainers can only view their own customers");
         }
         Users trainerAccount = getAccount(trainerId, "Trainer");
         if (trainerAccount.getRole() != Role.TRAINER) {
             throw new SecurityException("Selected user is not a trainer");
         }

         return java.util.stream.Stream.concat(
                         fitnessUserRepository.findByAssignedTrainer_Account_Id(trainerId).stream(),
                         java.util.stream.Stream.concat(
                                 fitnessUserRepository.findByAccount_Trainer_Id(trainerId).stream(),
                                 fitnessUserRepository.findByAccount_CreatedBy_Id(trainerId).stream()
                         )
                 )
                 .collect(java.util.stream.Collectors.toMap(
                         user -> user.getId(),
                         user -> user,
                         (left, right) -> left,
                         java.util.LinkedHashMap::new
                 ))
                 .values()
                 .stream()
                 .map(this::mapCustomerToResponse)
                 .collect(Collectors.toList());
     }

    @Override
    public CustomerResponse activateCustomer(Long customerId, Long trainerId) {
        Users trainerAccount = getAccount(trainerId, "Trainer");
        if (trainerAccount.getRole() != Role.TRAINER) {
            throw new SecurityException("Only Trainer can activate customers");
        }
        Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new RuntimeException("Trainer not found"));
        FitnessUser customer = fitnessUserRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        customer.getAccount().setIsActive(true);
        customer.getAccount().setIsApproved(true);
        customer.getAccount().setAdmin(trainerAccount.getAdmin() != null ? trainerAccount.getAdmin() : (trainerAccount.getRole() == Role.ADMIN ? trainerAccount : null));
        customer.getAccount().setManager(trainerAccount.getManager() != null ? trainerAccount.getManager() : (trainerAccount.getRole() == Role.MANAGER ? trainerAccount : null));
        customer.getAccount().setTrainer(trainerAccount);
        customer.setAssignedTrainer(trainer);
        FitnessUser saved = fitnessUserRepository.save(customer);
        // Let the approved member know they can now sign in.
        notificationService.createForUser(saved.getAccount(), "INFO", "Account activated",
                "Your account has been approved — you can now sign in and start training.", "/");
        return mapCustomerToResponse(saved);
    }

    @Override
    public CorporateHrResponse createCorporateHr(CreateCorporateHrRequest request, Long creatorId) {
        checkCreatePermission(creatorId, Role.CORPORATE_HR);
        Users creator = getAccount(creatorId, "Creator");
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        Users account = createAccount(request.getEmail(), request.getPassword(), request.getCompanyName(), null, Role.CORPORATE_HR, creator);
        account = userRepository.save(account);
        
        return CorporateHrResponse.builder()
                .id(account.getId())
                .companyName(account.getName())
                .email(account.getEmail())
                .role(account.getRole())
                .active(account.getIsActive())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    @Override
    public CorporateHrResponse updateCorporateHr(Long corporateHrId, CreateCorporateHrRequest request, Long updaterId) {
        checkUpdatePermission(updaterId, corporateHrId);
        Users account = userRepository.findById(corporateHrId)
                .orElseThrow(() -> new RuntimeException("Corporate HR not found"));
        
        account.setName(request.getCompanyName());
        if (!Boolean.TRUE.equals(request.getKeepPassword()) && request.getPassword() != null && !request.getPassword().isBlank()) {
            account.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        account = userRepository.save(account);
        
        return CorporateHrResponse.builder()
                .id(account.getId())
                .companyName(account.getName())
                .email(account.getEmail())
                .role(account.getRole())
                .active(account.getIsActive())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    @Override
    public void deleteCorporateHr(Long corporateHrId, Long deleterId) {
        checkDeletePermission(deleterId, corporateHrId);
        Users account = userRepository.findById(corporateHrId)
                .orElseThrow(() -> new RuntimeException("Corporate HR not found"));
        userRepository.delete(account);
    }

    @Override
    public List<CorporateHrResponse> getAllCorporateHrs(Long requesterId) {
        ensureRequesterCanList(requesterId, Role.CORPORATE_HR, "corporate hrs");
        return userRepository.findByRole(Role.CORPORATE_HR).stream()
                .map(account -> CorporateHrResponse.builder()
                        .id(account.getId())
                        .companyName(account.getName())
                        .email(account.getEmail())
                        .role(account.getRole())
                        .active(account.getIsActive())
                        .createdAt(account.getCreatedAt())
                        .updatedAt(account.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void setUserActiveStatus(Long userId, boolean active, Long updaterId) {
        // Role entities share their primary key with the account (@MapsId), so the
        // row id from the table IS the account id — operate on it directly.
        checkUpdatePermission(updaterId, userId);
        Users account = getAccount(userId, "User");
        account.setIsActive(active);
        // USER accounts also need approval to log in; activating from here approves them
        // so the Status toggle works as a single "enable login" control.
        if (active && account.getRole() == Role.USER) {
            account.setIsApproved(true);
        }
        userRepository.save(account);
        // Let an activated member know they can now sign in (skip self-toggles).
        if (active && account.getRole() == Role.USER && !updaterId.equals(userId)) {
            notificationService.createForUser(account, "INFO", "Account activated",
                    "Your account has been approved — you can now sign in and start training.", "/");
        }
    }

    @Override
    public DietPlan assignDietPlanToUser(Long userId, Long dietPlanId) {
        Users assigner = getAuthenticatedAccount();
        if (assigner.getRole() == Role.USER) {
            throw new SecurityException("You do not have permission to assign a diet plan to this user.");
        }

        FitnessUser customer = fitnessUserRepository.findById(userId)
                .orElseThrow(() -> new InvalidOperationException("Customer not found"));
        Users targetAccount = customer.getAccount();
        if (targetAccount.getRole() != Role.USER) {
            throw new SecurityException("You do not have permission to assign a diet plan to this user.");
        }

        if (!isVisibleToAssigner(assigner, targetAccount)) {
            throw new SecurityException("You do not have permission to assign a diet plan to this user.");
        }

        DietPlan dietPlan = dietPlanRepository.findById(dietPlanId)
                .orElseThrow(() -> new InvalidOperationException("Diet plan not found"));
        targetAccount.setAssignedDietPlan(dietPlan);
        fitnessUserRepository.save(customer);
        notificationService.createForUser(targetAccount, "DIET", "New diet plan assigned",
                dietPlan.getName() + " was assigned to you by " + assigner.getName(), "/dietplan");
        return dietPlan;
    }

    @Override
    @Transactional(readOnly = true)
    public DietPlan getMyDietPlan() {
        Users currentUser = getAuthenticatedAccount();
        DietPlan assignedDietPlan = currentUser.getAssignedDietPlan();
        if (assignedDietPlan == null || assignedDietPlan.getId() == null) {
            return null;
        }

        return dietPlanRepository.findById(assignedDietPlan.getId()).orElse(null);
    }

    @Override
    public WorkoutPlan assignWorkoutPlanToUser(Long userId, Long workoutPlanId) {
        Users assigner = getAuthenticatedAccount();
        if (assigner.getRole() == Role.USER) {
            throw new SecurityException("You do not have permission to assign a workout plan to this user.");
        }

        FitnessUser customer = fitnessUserRepository.findById(userId)
                .orElseThrow(() -> new InvalidOperationException("Customer not found"));
        Users targetAccount = customer.getAccount();
        if (targetAccount.getRole() != Role.USER) {
            throw new SecurityException("You do not have permission to assign a workout plan to this user.");
        }

        if (!isVisibleToAssigner(assigner, targetAccount)) {
            throw new SecurityException("You do not have permission to assign a workout plan to this user.");
        }

        WorkoutPlan workoutPlan = workoutPlanRepository.findById(workoutPlanId)
                .orElseThrow(() -> new InvalidOperationException("Workout plan not found"));
        targetAccount.setAssignedWorkoutPlan(workoutPlan);
        fitnessUserRepository.save(customer);
        notificationService.createForUser(targetAccount, "WORKOUT", "New workout plan assigned",
                workoutPlan.getName() + " was assigned to you by " + assigner.getName(), "/workout-detail");
        return workoutPlan;
    }

    @Override
    @Transactional(readOnly = true)
    public WorkoutPlan getMyWorkoutPlan() {
        Users currentUser = getAuthenticatedAccount();
        WorkoutPlan assignedWorkoutPlan = currentUser.getAssignedWorkoutPlan();
        if (assignedWorkoutPlan == null || assignedWorkoutPlan.getId() == null) {
            return null;
        }

        return workoutPlanRepository.findById(assignedWorkoutPlan.getId()).orElse(null);
    }

    @Transactional(readOnly = true)
    public MyProfileResponse getMyProfile() {
        Users user = getAuthenticatedAccount();
        FitnessUser fitness = fitnessUserRepository.findById(user.getId()).orElse(null);
        return buildProfileResponse(user, fitness);
    }

    /** Marks the signed-in member's onboarding as finished (drives the staff notification). */
    public void completeOnboarding() {
        Users user = getAuthenticatedAccount();
        user.setOnboardingCompletedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public MyProfileResponse updateMyProfile(MyProfileRequest request) {
        Users user = getAuthenticatedAccount();
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName().trim());
            userRepository.save(user);
        }

        FitnessUser fitness = fitnessUserRepository.findById(user.getId()).orElse(null);
        if (fitness == null && user.getRole() == Role.USER) {
            fitness = new FitnessUser();
            fitness.setAccount(user);
        }

        if (fitness != null) {
            fitness.setWeight(request.getWeight());
            fitness.setHeight(request.getHeight());
            fitness.setAge(request.getAge());
            fitness.setGender(request.getGender());
            fitness.setDateOfBirth(request.getDateOfBirth() != null ? request.getDateOfBirth().atStartOfDay() : null);
            fitness.setBloodGroup(request.getBloodGroup());
            fitness.setPhone(request.getPhone());
            fitness.setAddress(request.getAddress());
            fitness.setCity(request.getCity());
            fitness.setBio(request.getBio());
            fitness.setFitnessGoals(request.getFitnessGoals());
            fitness = fitnessUserRepository.save(fitness);
        }

        return buildProfileResponse(user, fitness);
    }

    private MyProfileResponse buildProfileResponse(Users user, FitnessUser fitness) {
        return new MyProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole() == null ? null : user.getRole().name(),
                fitness == null ? null : fitness.getWeight(),
                fitness == null ? null : fitness.getHeight(),
                fitness == null ? null : fitness.getAge(),
                fitness == null ? null : fitness.getGender(),
                fitness == null || fitness.getDateOfBirth() == null ? null : fitness.getDateOfBirth().toLocalDate(),
                fitness == null ? null : fitness.getBloodGroup(),
                fitness == null ? null : fitness.getPhone(),
                fitness == null ? null : fitness.getAddress(),
                fitness == null ? null : fitness.getCity(),
                fitness == null ? null : fitness.getBio(),
                fitness == null ? null : fitness.getFitnessGoals(),
                fitness != null
        );
    }

    private List<FitnessUser> getVisibleCustomersForRequester(Users requester) {
        if (requester == null) {
            throw new SecurityException("Requester not found");
        }

        switch (requester.getRole()) {
            case SUPER_ADMIN:
                return fitnessUserRepository.findAll();
            case ADMIN:
                return fitnessUserRepository.findByAccount_Admin_Id(requester.getId());
            case MANAGER:
                return fitnessUserRepository.findByAccount_Manager_Id(requester.getId());
            case TRAINER:
                return java.util.stream.Stream.concat(
                    fitnessUserRepository.findByAssignedTrainer_Account_Id(requester.getId()).stream(),
                    java.util.stream.Stream.concat(
                            fitnessUserRepository.findByAccount_Trainer_Id(requester.getId()).stream(),
                            fitnessUserRepository.findByAccount_CreatedBy_Id(requester.getId()).stream()
                    )
                        )
                        .collect(java.util.stream.Collectors.toMap(
                                user -> user.getId(),
                                user -> user,
                                (left, right) -> left,
                                java.util.LinkedHashMap::new
                        ))
                        .values()
                        .stream()
                        .toList();
            default:
                throw new SecurityException("You do not have permission to view customers");
        }
    }

    private boolean isVisibleToAssigner(Users assigner, Users targetAccount) {
        if (assigner == null || targetAccount == null) {
            return false;
        }

        if (assigner.getRole() == Role.SUPER_ADMIN) {
            return true;
        }

        if (targetAccount.getRole() != Role.USER) {
            return false;
        }

        switch (assigner.getRole()) {
            case ADMIN:
                return targetAccount.getAdmin() != null && targetAccount.getAdmin().getId().equals(assigner.getId());
            case MANAGER:
                return targetAccount.getManager() != null && targetAccount.getManager().getId().equals(assigner.getId());
            case TRAINER:
                return targetAccount.getTrainer() != null && targetAccount.getTrainer().getId().equals(assigner.getId());
            default:
                return false;
        }
    }

    private void copyAdminFields(Admin admin, CreateAdminRequest request) {
        Users account = admin.getAccount();
        applyOrgFields(account, request.getHeadOfficeId(), request.getBranchId(), request.getDepartmentId(), request.getTeamId(), request.getDesignationId());
        applyReportsTo(account, request.getReportsToId());
        BeanUtils.copyProperties(request, admin, "dateOfBirth", "probationEndDate", "declarationDate", "joinDate", "reportsToId", "password");
        admin.setFirstName(request.getFirstName());
        admin.setLastName(normalizeOptional(request.getLastName()));
        admin.setDepartment(request.getDepartment());
        admin.setPhone(request.getPhone());
        admin.setEmployeeId(request.getEmployeeId());
        admin.setQualification(request.getQualification());
        admin.setBio(request.getBio());
        parseJoinDate(request.getJoinDate()).ifPresent(admin::setJoinDate);
        copyAdminProfessionalFields(admin, request);
    }

    private void copyManagerFields(Manager manager, CreateAdminRequest request) {
        Users account = manager.getAccount();
        applyOrgFields(account, request.getHeadOfficeId(), request.getBranchId(), request.getDepartmentId(), request.getTeamId(), request.getDesignationId());
        applyReportsTo(account, request.getReportsToId());
        BeanUtils.copyProperties(request, manager, "dateOfBirth", "probationEndDate", "declarationDate", "joinDate", "reportsToId", "password");
        manager.setFirstName(request.getFirstName());
        manager.setLastName(normalizeOptional(request.getLastName()));
        manager.setDepartment(request.getDepartment());
        manager.setPhone(request.getPhone());
        manager.setEmployeeId(request.getEmployeeId());
        manager.setQualification(request.getQualification());
        manager.setBio(request.getBio());
        parseJoinDate(request.getJoinDate()).ifPresent(manager::setJoinDate);
        copyManagerProfessionalFields(manager, request);
    }

    private void copyTrainerFields(Trainer trainer, CreateTrainerRequest request) {
        Users account = trainer.getAccount();
        applyOrgFields(account, request.getHeadOfficeId(), request.getBranchId(), request.getDepartmentId(), request.getTeamId(), request.getDesignationId());
        applyReportsTo(account, request.getReportsToId());
        BeanUtils.copyProperties(request, trainer, "dateOfBirth", "probationEndDate", "declarationDate", "joinDate", "reportsToId", "password");
        trainer.setFirstName(request.getFirstName());
        trainer.setLastName(normalizeOptional(request.getLastName()));
        trainer.setSpecialization(request.getSpecialization());
        trainer.setExperienceYears(request.getExperienceYears());
        trainer.setCertification(request.getCertification());
        trainer.setPhone(request.getPhone());
        trainer.setQualification(request.getQualification());
        trainer.setRatePerHour(request.getRatePerHour());
        trainer.setBio(request.getBio());
        trainer.setLanguages(request.getLanguages());
        if (request.getRating() != null) {
            trainer.setRating(request.getRating());
        }
        if (request.getTotalClientsTrained() != null) {
            trainer.setTotalClientsTrained(request.getTotalClientsTrained());
        }
        parseJoinDate(request.getJoinDate()).ifPresent(trainer::setJoinDate);
        copyTrainerProfessionalFields(trainer, request);
    }


    private java.util.Optional<LocalDate> parseDate(String value) {
        if (value == null || value.isBlank()) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(LocalDate.parse(value));
    }

    private void copyAdminProfessionalFields(Admin admin, CreateAdminRequest request) {
        parseDate(request.getDateOfBirth()).ifPresent(admin::setDateOfBirth);
        parseDate(request.getProbationEndDate()).ifPresent(admin::setProbationEndDate);
        parseDate(request.getDeclarationDate()).ifPresent(admin::setDeclarationDate);
        admin.setGender(normalizeOptional(request.getGender()));
        admin.setBloodGroup(normalizeOptional(request.getBloodGroup()));
        admin.setPersonalEmail(normalizeOptional(request.getPersonalEmail()));
        admin.setAlternatePhone(normalizeOptional(request.getAlternatePhone()));
        admin.setEmergencyContact(normalizeOptional(request.getEmergencyContact()));
        admin.setEmergencyPhone(normalizeOptional(request.getEmergencyPhone()));
        admin.setCurrentAddress(normalizeOptional(request.getCurrentAddress()));
        admin.setPermanentAddress(normalizeOptional(request.getPermanentAddress()));
        admin.setCity(normalizeOptional(request.getCity()));
        admin.setState(normalizeOptional(request.getState()));
        admin.setPincode(normalizeOptional(request.getPincode()));
        admin.setEmploymentType(normalizeOptional(request.getEmploymentType()));
        admin.setWorkLocation(normalizeOptional(request.getWorkLocation()));
        admin.setReportingManagerName(normalizeOptional(request.getReportingManagerName()));
        admin.setPanNumber(normalizeOptional(request.getPanNumber()));
        admin.setAadharNumber(normalizeOptional(request.getAadharNumber()));
        admin.setBankName(normalizeOptional(request.getBankName()));
        admin.setBankAccountNumber(normalizeOptional(request.getBankAccountNumber()));
        admin.setBankIfscCode(normalizeOptional(request.getBankIfscCode()));
        admin.setBankAccountType(normalizeOptional(request.getBankAccountType()));
        admin.setQualificationDocumentPath(normalizeOptional(request.getQualificationDocumentPath()));
        admin.setCertificationDocumentPath(normalizeOptional(request.getCertificationDocumentPath()));
        admin.setIdProofDocumentPath(normalizeOptional(request.getIdProofDocumentPath()));
        admin.setAddressProofDocumentPath(normalizeOptional(request.getAddressProofDocumentPath()));
        admin.setResumeDocumentPath(normalizeOptional(request.getResumeDocumentPath()));
        admin.setOfferLetterDocumentPath(normalizeOptional(request.getOfferLetterDocumentPath()));
        admin.setBankAccountHolderName(normalizeOptional(request.getBankAccountHolderName()));
        admin.setBankBranch(normalizeOptional(request.getBankBranch()));
        admin.setCandidatePhotoPath(normalizeOptional(request.getCandidatePhotoPath()));
        admin.setAadharCardDocumentPath(normalizeOptional(request.getAadharCardDocumentPath()));
        admin.setPanCardDocumentPath(normalizeOptional(request.getPanCardDocumentPath()));
        admin.setBankDocumentPath(normalizeOptional(request.getBankDocumentPath()));
        admin.setPreviousEmployment1(normalizeOptional(request.getPreviousEmployment1()));
        admin.setPreviousEmployment2(normalizeOptional(request.getPreviousEmployment2()));
        admin.setExperienceCertificateDocumentPath(normalizeOptional(request.getExperienceCertificateDocumentPath()));
        admin.setCourseCertificatePath(normalizeOptional(request.getCourseCertificatePath()));
        admin.setEducationCertificatePath(normalizeOptional(request.getEducationCertificatePath()));
        admin.setFatherName(normalizeOptional(request.getFatherName()));
        admin.setMotherName(normalizeOptional(request.getMotherName()));
        admin.setMaritalStatus(normalizeOptional(request.getMaritalStatus()));
        admin.setSpouseName(normalizeOptional(request.getSpouseName()));
        admin.setLocation(normalizeOptional(request.getLocation()));
        admin.setEmergencyContactRelationship(normalizeOptional(request.getEmergencyContactRelationship()));
        admin.setEmergencyContactName2(normalizeOptional(request.getEmergencyContactName2()));
        admin.setEmergencyContactRelationship2(normalizeOptional(request.getEmergencyContactRelationship2()));
        admin.setEmergencyPhone2(normalizeOptional(request.getEmergencyPhone2()));
        admin.setReferenceName1(normalizeOptional(request.getReferenceName1()));
        admin.setReferencePhone1(normalizeOptional(request.getReferencePhone1()));
        admin.setReferenceName2(normalizeOptional(request.getReferenceName2()));
        admin.setReferencePhone2(normalizeOptional(request.getReferencePhone2()));
        admin.setJoiningBranchName(normalizeOptional(request.getJoiningBranchName()));
        admin.setSourcePlatform(normalizeOptional(request.getSourcePlatform()));
        admin.setPfUan(normalizeOptional(request.getPfUan()));
        admin.setEsiNumber(normalizeOptional(request.getEsiNumber()));
        admin.setDeclarationPlace(normalizeOptional(request.getDeclarationPlace()));
    }

    private void copyManagerProfessionalFields(Manager manager, CreateAdminRequest request) {
        parseDate(request.getDateOfBirth()).ifPresent(manager::setDateOfBirth);
        parseDate(request.getProbationEndDate()).ifPresent(manager::setProbationEndDate);
        parseDate(request.getDeclarationDate()).ifPresent(manager::setDeclarationDate);
        manager.setGender(normalizeOptional(request.getGender()));
        manager.setBloodGroup(normalizeOptional(request.getBloodGroup()));
        manager.setPersonalEmail(normalizeOptional(request.getPersonalEmail()));
        manager.setAlternatePhone(normalizeOptional(request.getAlternatePhone()));
        manager.setEmergencyContact(normalizeOptional(request.getEmergencyContact()));
        manager.setEmergencyPhone(normalizeOptional(request.getEmergencyPhone()));
        manager.setCurrentAddress(normalizeOptional(request.getCurrentAddress()));
        manager.setPermanentAddress(normalizeOptional(request.getPermanentAddress()));
        manager.setCity(normalizeOptional(request.getCity()));
        manager.setState(normalizeOptional(request.getState()));
        manager.setPincode(normalizeOptional(request.getPincode()));
        manager.setEmploymentType(normalizeOptional(request.getEmploymentType()));
        manager.setWorkLocation(normalizeOptional(request.getWorkLocation()));
        manager.setReportingManagerName(normalizeOptional(request.getReportingManagerName()));
        manager.setPanNumber(normalizeOptional(request.getPanNumber()));
        manager.setAadharNumber(normalizeOptional(request.getAadharNumber()));
        manager.setBankName(normalizeOptional(request.getBankName()));
        manager.setBankAccountNumber(normalizeOptional(request.getBankAccountNumber()));
        manager.setBankIfscCode(normalizeOptional(request.getBankIfscCode()));
        manager.setBankAccountType(normalizeOptional(request.getBankAccountType()));
        manager.setQualificationDocumentPath(normalizeOptional(request.getQualificationDocumentPath()));
        manager.setCertificationDocumentPath(normalizeOptional(request.getCertificationDocumentPath()));
        manager.setIdProofDocumentPath(normalizeOptional(request.getIdProofDocumentPath()));
        manager.setAddressProofDocumentPath(normalizeOptional(request.getAddressProofDocumentPath()));
        manager.setResumeDocumentPath(normalizeOptional(request.getResumeDocumentPath()));
        manager.setOfferLetterDocumentPath(normalizeOptional(request.getOfferLetterDocumentPath()));
        manager.setBankAccountHolderName(normalizeOptional(request.getBankAccountHolderName()));
        manager.setBankBranch(normalizeOptional(request.getBankBranch()));
        manager.setCandidatePhotoPath(normalizeOptional(request.getCandidatePhotoPath()));
        manager.setAadharCardDocumentPath(normalizeOptional(request.getAadharCardDocumentPath()));
        manager.setPanCardDocumentPath(normalizeOptional(request.getPanCardDocumentPath()));
        manager.setBankDocumentPath(normalizeOptional(request.getBankDocumentPath()));
        manager.setPreviousEmployment1(normalizeOptional(request.getPreviousEmployment1()));
        manager.setPreviousEmployment2(normalizeOptional(request.getPreviousEmployment2()));
        manager.setExperienceCertificateDocumentPath(normalizeOptional(request.getExperienceCertificateDocumentPath()));
        manager.setCourseCertificatePath(normalizeOptional(request.getCourseCertificatePath()));
        manager.setEducationCertificatePath(normalizeOptional(request.getEducationCertificatePath()));
        manager.setFatherName(normalizeOptional(request.getFatherName()));
        manager.setMotherName(normalizeOptional(request.getMotherName()));
        manager.setMaritalStatus(normalizeOptional(request.getMaritalStatus()));
        manager.setSpouseName(normalizeOptional(request.getSpouseName()));
        manager.setLocation(normalizeOptional(request.getLocation()));
        manager.setEmergencyContactRelationship(normalizeOptional(request.getEmergencyContactRelationship()));
        manager.setEmergencyContactName2(normalizeOptional(request.getEmergencyContactName2()));
        manager.setEmergencyContactRelationship2(normalizeOptional(request.getEmergencyContactRelationship2()));
        manager.setEmergencyPhone2(normalizeOptional(request.getEmergencyPhone2()));
        manager.setReferenceName1(normalizeOptional(request.getReferenceName1()));
        manager.setReferencePhone1(normalizeOptional(request.getReferencePhone1()));
        manager.setReferenceName2(normalizeOptional(request.getReferenceName2()));
        manager.setReferencePhone2(normalizeOptional(request.getReferencePhone2()));
        manager.setJoiningBranchName(normalizeOptional(request.getJoiningBranchName()));
        manager.setSourcePlatform(normalizeOptional(request.getSourcePlatform()));
        manager.setPfUan(normalizeOptional(request.getPfUan()));
        manager.setEsiNumber(normalizeOptional(request.getEsiNumber()));
        manager.setDeclarationPlace(normalizeOptional(request.getDeclarationPlace()));
    }

    private void copyTrainerProfessionalFields(Trainer trainer, CreateTrainerRequest request) {
        parseDate(request.getDateOfBirth()).ifPresent(trainer::setDateOfBirth);
        parseDate(request.getProbationEndDate()).ifPresent(trainer::setProbationEndDate);
        parseDate(request.getDeclarationDate()).ifPresent(trainer::setDeclarationDate);
        trainer.setGender(normalizeOptional(request.getGender()));
        trainer.setBloodGroup(normalizeOptional(request.getBloodGroup()));
        trainer.setPersonalEmail(normalizeOptional(request.getPersonalEmail()));
        trainer.setAlternatePhone(normalizeOptional(request.getAlternatePhone()));
        trainer.setEmergencyContact(normalizeOptional(request.getEmergencyContact()));
        trainer.setEmergencyPhone(normalizeOptional(request.getEmergencyPhone()));
        trainer.setCurrentAddress(normalizeOptional(request.getCurrentAddress()));
        trainer.setPermanentAddress(normalizeOptional(request.getPermanentAddress()));
        trainer.setCity(normalizeOptional(request.getCity()));
        trainer.setState(normalizeOptional(request.getState()));
        trainer.setPincode(normalizeOptional(request.getPincode()));
        trainer.setEmploymentType(normalizeOptional(request.getEmploymentType()));
        trainer.setWorkLocation(normalizeOptional(request.getWorkLocation()));
        trainer.setReportingManagerName(normalizeOptional(request.getReportingManagerName()));
        trainer.setPanNumber(normalizeOptional(request.getPanNumber()));
        trainer.setAadharNumber(normalizeOptional(request.getAadharNumber()));
        trainer.setBankName(normalizeOptional(request.getBankName()));
        trainer.setBankAccountNumber(normalizeOptional(request.getBankAccountNumber()));
        trainer.setBankIfscCode(normalizeOptional(request.getBankIfscCode()));
        trainer.setBankAccountType(normalizeOptional(request.getBankAccountType()));
        trainer.setQualificationDocumentPath(normalizeOptional(request.getQualificationDocumentPath()));
        trainer.setCertificationDocumentPath(normalizeOptional(request.getCertificationDocumentPath()));
        trainer.setIdProofDocumentPath(normalizeOptional(request.getIdProofDocumentPath()));
        trainer.setAddressProofDocumentPath(normalizeOptional(request.getAddressProofDocumentPath()));
        trainer.setResumeDocumentPath(normalizeOptional(request.getResumeDocumentPath()));
        trainer.setOfferLetterDocumentPath(normalizeOptional(request.getOfferLetterDocumentPath()));
        trainer.setBankAccountHolderName(normalizeOptional(request.getBankAccountHolderName()));
        trainer.setBankBranch(normalizeOptional(request.getBankBranch()));
        trainer.setCandidatePhotoPath(normalizeOptional(request.getCandidatePhotoPath()));
        trainer.setAadharCardDocumentPath(normalizeOptional(request.getAadharCardDocumentPath()));
        trainer.setPanCardDocumentPath(normalizeOptional(request.getPanCardDocumentPath()));
        trainer.setBankDocumentPath(normalizeOptional(request.getBankDocumentPath()));
        trainer.setPreviousEmployment1(normalizeOptional(request.getPreviousEmployment1()));
        trainer.setPreviousEmployment2(normalizeOptional(request.getPreviousEmployment2()));
        trainer.setExperienceCertificateDocumentPath(normalizeOptional(request.getExperienceCertificateDocumentPath()));
        trainer.setCourseCertificatePath(normalizeOptional(request.getCourseCertificatePath()));
        trainer.setEducationCertificatePath(normalizeOptional(request.getEducationCertificatePath()));
        trainer.setFatherName(normalizeOptional(request.getFatherName()));
        trainer.setMotherName(normalizeOptional(request.getMotherName()));
        trainer.setMaritalStatus(normalizeOptional(request.getMaritalStatus()));
        trainer.setSpouseName(normalizeOptional(request.getSpouseName()));
        trainer.setLocation(normalizeOptional(request.getLocation()));
        trainer.setEmergencyContactRelationship(normalizeOptional(request.getEmergencyContactRelationship()));
        trainer.setEmergencyContactName2(normalizeOptional(request.getEmergencyContactName2()));
        trainer.setEmergencyContactRelationship2(normalizeOptional(request.getEmergencyContactRelationship2()));
        trainer.setEmergencyPhone2(normalizeOptional(request.getEmergencyPhone2()));
        trainer.setReferenceName1(normalizeOptional(request.getReferenceName1()));
        trainer.setReferencePhone1(normalizeOptional(request.getReferencePhone1()));
        trainer.setReferenceName2(normalizeOptional(request.getReferenceName2()));
        trainer.setReferencePhone2(normalizeOptional(request.getReferencePhone2()));
        trainer.setJoiningBranchName(normalizeOptional(request.getJoiningBranchName()));
        trainer.setSourcePlatform(normalizeOptional(request.getSourcePlatform()));
        trainer.setPfUan(normalizeOptional(request.getPfUan()));
        trainer.setEsiNumber(normalizeOptional(request.getEsiNumber()));
        trainer.setDeclarationPlace(normalizeOptional(request.getDeclarationPlace()));
    }

    private void applyOrgFields(Users account, Long headOfficeId, Long branchId, Long departmentId, Long teamId, Long designationId) {
        account.setHeadOfficeId(headOfficeId);
        account.setBranchId(branchId);
        account.setDepartmentId(departmentId);
        account.setTeamId(teamId);
        account.setDesignationId(designationId);
    }

    private java.util.Optional<LocalDateTime> parseJoinDate(String value) {
        if (value == null || value.isBlank()) {
            return java.util.Optional.empty();
        }
        try {
            return java.util.Optional.of(LocalDate.parse(value).atStartOfDay());
        } catch (Exception ignored) {
            try {
                return java.util.Optional.of(LocalDateTime.parse(value));
            } catch (Exception ignoredAgain) {
                return java.util.Optional.empty();
            }
        }
    }

    private void copyCustomerFields(FitnessUser customer, CreateCustomerByTrainerRequest request) {
        customer.setWeight(request.getWeight());
        customer.setHeight(request.getHeight());
        customer.setBloodGroup(request.getBloodGroup());
        customer.setAge(request.getAge());
        customer.setGender(request.getGender());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        customer.setCity(request.getCity());
        customer.setMedicalConditions(request.getMedicalConditions());
        customer.setEmergencyContact(request.getEmergencyContact());
        customer.setEmergencyPhone(request.getEmergencyPhone());
        customer.setPhotoPath(request.getPhotoPath());
        customer.setIdProofPath(request.getIdProofPath());
        customer.setBodyFat(request.getBodyFat());
        customer.setIsFrozen(request.getIsFrozen() != null ? request.getIsFrozen() : false);
        customer.setReferredBy(request.getReferredBy());
    }

    private void copyCustomerFields(FitnessUser customer, UpdateCustomerDetailsRequest request) {
        customer.setWeight(request.getWeight());
        customer.setHeight(request.getHeight());
        customer.setBloodGroup(request.getBloodGroup());
        customer.setAge(request.getAge());
        customer.setGender(request.getGender());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        customer.setCity(request.getCity());
        customer.setMedicalConditions(request.getMedicalConditions());
        customer.setEmergencyContact(request.getEmergencyContact());
        customer.setEmergencyPhone(request.getEmergencyPhone());
        customer.setBodyFat(request.getBodyFat());
        customer.setIsFrozen(request.getIsFrozen() != null ? request.getIsFrozen() : false);
        customer.setReferredBy(request.getReferredBy());
        // Only overwrite documents when the update actually carries a new path, so
        // an edit that doesn't re-upload keeps the existing photo / ID proof.
        if (request.getPhotoPath() != null && !request.getPhotoPath().isBlank()) {
            customer.setPhotoPath(request.getPhotoPath());
        }
        if (request.getIdProofPath() != null && !request.getIdProofPath().isBlank()) {
            customer.setIdProofPath(request.getIdProofPath());
        }
    }

    private SuperAdminResponse mapSuperAdminToResponse(SuperAdmin superAdmin) {
        Users account = superAdmin.getAccount();
        return new SuperAdminResponse(
                account.getId(),
                account.getEmail(),
                superAdmin.getFirstName(),
                superAdmin.getLastName(),
                account.getIsActive()
        );
    }


    private void applyReportsToResponse(AdminResponse response, Users account) {
        Users reportsTo = account.getReportsTo();
        if (reportsTo != null) {
            response.setReportsToId(reportsTo.getId());
            response.setReportsToName(reportsTo.getName());
            response.setReportsToRole(reportsTo.getRole().name());
            response.setReportingManagerName(reportsTo.getName());
        }
    }

    private void applyReportsToResponse(TrainerResponse response, Users account) {
        Users reportsTo = account.getReportsTo();
        if (reportsTo != null) {
            response.setReportsToId(reportsTo.getId());
            response.setReportsToName(reportsTo.getName());
            response.setReportsToRole(reportsTo.getRole().name());
            response.setReportingManagerName(reportsTo.getName());
        }
    }

    private void enrichAdminResponse(AdminResponse response, Users account, Admin admin) {
        BeanUtils.copyProperties(admin, response, "id", "account", "dateOfBirth", "probationEndDate", "declarationDate");
        response.setDateOfBirth(toDateString(admin.getDateOfBirth()));
        response.setProbationEndDate(toDateString(admin.getProbationEndDate()));
        response.setDeclarationDate(toDateString(admin.getDeclarationDate()));
        applyReportsToResponse(response, account);
    }

    private void enrichManagerResponse(AdminResponse response, Users account, Manager manager) {
        BeanUtils.copyProperties(manager, response, "id", "account", "dateOfBirth", "probationEndDate", "declarationDate");
        response.setDateOfBirth(toDateString(manager.getDateOfBirth()));
        response.setProbationEndDate(toDateString(manager.getProbationEndDate()));
        response.setDeclarationDate(toDateString(manager.getDeclarationDate()));
        applyReportsToResponse(response, account);
    }

    private void enrichTrainerResponse(TrainerResponse response, Users account, Trainer trainer) {
        BeanUtils.copyProperties(trainer, response, "id", "account", "assignedUsers", "dateOfBirth", "probationEndDate", "declarationDate");
        response.setDateOfBirth(toDateString(trainer.getDateOfBirth()));
        response.setProbationEndDate(toDateString(trainer.getProbationEndDate()));
        response.setDeclarationDate(toDateString(trainer.getDeclarationDate()));
        applyReportsToResponse(response, account);
    }

    private AdminResponse mapAdminToResponse(Admin admin) {
        Users account = admin.getAccount();
        AdminResponse response = new AdminResponse(
                account.getId(),
                account.getEmail(),
                admin.getFirstName(),
                admin.getLastName(),
                admin.getDepartment(),
                admin.getPhone(),
                admin.getEmployeeId(),
                admin.getQualification(),
                account.getIsActive(),
                account.getCreatedBy() != null ? account.getCreatedBy().getName() : null,
                account.getHeadOfficeId(),
                account.getBranchId(),
                account.getDepartmentId(),
                account.getTeamId(),
                account.getDesignationId(),
                admin.getJoinDate(),
                admin.getBio(),
                toDateString(admin.getDateOfBirth()),
                admin.getGender(),
                admin.getBloodGroup(),
                admin.getPersonalEmail(),
                admin.getAlternatePhone(),
                admin.getEmergencyContact(),
                admin.getEmergencyPhone(),
                admin.getCurrentAddress(),
                admin.getPermanentAddress(),
                admin.getCity(),
                admin.getState(),
                admin.getPincode(),
                admin.getEmploymentType(),
                admin.getWorkLocation(),
                admin.getReportingManagerName(),
                toDateString(admin.getProbationEndDate()),
                admin.getPanNumber(),
                admin.getAadharNumber(),
                admin.getBankName(),
                admin.getBankAccountNumber(),
                admin.getBankIfscCode(),
                admin.getBankAccountType(),
                admin.getQualificationDocumentPath(),
                admin.getCertificationDocumentPath(),
                admin.getIdProofDocumentPath(),
                admin.getAddressProofDocumentPath(),
                admin.getResumeDocumentPath(),
                admin.getOfferLetterDocumentPath(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
        enrichAdminResponse(response, account, admin);
        return response;
    }

    private AdminResponse mapManagerToResponse(Manager manager) {
        Users account = manager.getAccount();
        AdminResponse response = new AdminResponse(
                account.getId(),
                account.getEmail(),
                manager.getFirstName(),
                manager.getLastName(),
                manager.getDepartment(),
                manager.getPhone(),
                manager.getEmployeeId(),
                manager.getQualification(),
                account.getIsActive(),
                account.getCreatedBy() != null ? account.getCreatedBy().getName() : null,
                account.getHeadOfficeId(),
                account.getBranchId(),
                account.getDepartmentId(),
                account.getTeamId(),
                account.getDesignationId(),
                manager.getJoinDate(),
                manager.getBio(),
                toDateString(manager.getDateOfBirth()),
                manager.getGender(),
                manager.getBloodGroup(),
                manager.getPersonalEmail(),
                manager.getAlternatePhone(),
                manager.getEmergencyContact(),
                manager.getEmergencyPhone(),
                manager.getCurrentAddress(),
                manager.getPermanentAddress(),
                manager.getCity(),
                manager.getState(),
                manager.getPincode(),
                manager.getEmploymentType(),
                manager.getWorkLocation(),
                manager.getReportingManagerName(),
                toDateString(manager.getProbationEndDate()),
                manager.getPanNumber(),
                manager.getAadharNumber(),
                manager.getBankName(),
                manager.getBankAccountNumber(),
                manager.getBankIfscCode(),
                manager.getBankAccountType(),
                manager.getQualificationDocumentPath(),
                manager.getCertificationDocumentPath(),
                manager.getIdProofDocumentPath(),
                manager.getAddressProofDocumentPath(),
                manager.getResumeDocumentPath(),
                manager.getOfferLetterDocumentPath(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
        enrichManagerResponse(response, account, manager);
        return response;
    }

    private TrainerResponse mapTrainerToResponse(Trainer trainer) {
        Users account = trainer.getAccount();
        TrainerResponse response = new TrainerResponse(
                account.getId(),
                account.getEmail(),
                trainer.getFirstName(),
                trainer.getLastName(),
                trainer.getSpecialization(),
                trainer.getExperienceYears(),
                trainer.getCertification(),
                trainer.getPhone(),
                trainer.getQualification(),
                trainer.getRatePerHour(),
                account.getIsActive(),
                account.getCreatedBy() != null ? account.getCreatedBy().getName() : null,
                account.getHeadOfficeId(),
                account.getBranchId(),
                account.getDepartmentId(),
                account.getTeamId(),
                account.getDesignationId(),
                trainer.getJoinDate(),
                trainer.getBio(),
                trainer.getLanguages(),
                trainer.getRating(),
                trainer.getTotalClientsTrained(),
                toDateString(trainer.getDateOfBirth()),
                trainer.getGender(),
                trainer.getBloodGroup(),
                trainer.getPersonalEmail(),
                trainer.getAlternatePhone(),
                trainer.getEmergencyContact(),
                trainer.getEmergencyPhone(),
                trainer.getCurrentAddress(),
                trainer.getPermanentAddress(),
                trainer.getCity(),
                trainer.getState(),
                trainer.getPincode(),
                trainer.getEmploymentType(),
                trainer.getWorkLocation(),
                trainer.getReportingManagerName(),
                toDateString(trainer.getProbationEndDate()),
                trainer.getPanNumber(),
                trainer.getAadharNumber(),
                trainer.getBankName(),
                trainer.getBankAccountNumber(),
                trainer.getBankIfscCode(),
                trainer.getBankAccountType(),
                trainer.getQualificationDocumentPath(),
                trainer.getCertificationDocumentPath(),
                trainer.getIdProofDocumentPath(),
                trainer.getAddressProofDocumentPath(),
                trainer.getResumeDocumentPath(),
                trainer.getOfferLetterDocumentPath(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
        enrichTrainerResponse(response, account, trainer);
        return response;
    }

    private CustomerResponse mapCustomerToResponse(FitnessUser customer) {
        Users account = customer.getAccount();
        return new CustomerResponse(
                account.getId(),
                account.getEmail(),
                customer.getFirstName(),
                customer.getLastName(),
                account.getHeadOfficeId(),
                account.getBranchId(),
                account.getDepartmentId(),
                account.getTeamId(),
                account.getDesignationId(),
                customer.getWeight(),
                customer.getHeight(),
                customer.getBloodGroup(),
                customer.getAge(),
                customer.getGender(),
                customer.getPhone(),
                customer.getAddress(),
                customer.getCity(),
                customer.getMedicalConditions(),
                customer.getEmergencyContact(),
                customer.getEmergencyPhone(),
                account.getIsActive(),
                account.getIsApproved(),
                customer.getAssignedTrainer() != null ? customer.getAssignedTrainer().getAccount().getId() : null,
                customer.getAssignedTrainer() != null ? customer.getAssignedTrainer().getAccount().getName() : null,
                account.getTrainer() != null ? account.getTrainer().getId() : null,
                account.getCreatedBy() != null ? account.getCreatedBy().getId() : null,
                customer.getPhotoPath(),
                customer.getIdProofPath(),
                account.getAssignedDietPlan() != null ? account.getAssignedDietPlan().getId() : null,
                account.getAssignedDietPlan() != null ? account.getAssignedDietPlan().getName() : null,
                account.getAssignedWorkoutPlan() != null ? account.getAssignedWorkoutPlan().getId() : null,
                account.getAssignedWorkoutPlan() != null ? account.getAssignedWorkoutPlan().getName() : null,
                customer.getBodyFat(),
                customer.getIsFrozen(),
                customer.getReferredBy(),
                customer.getMembershipExpiry(),
                customer.getMembershipPlanRef() != null ? customer.getMembershipPlanRef().getName() : (customer.getMembershipPlan() != null ? customer.getMembershipPlan() : "BASIC"),
                customer.getMembershipPlanRef() != null ? customer.getMembershipPlanRef().getId() : null,
                customer.getAccessStartTime() != null ? customer.getAccessStartTime().toString() : null,
                customer.getAccessEndTime() != null ? customer.getAccessEndTime().toString() : null
        );
    }

    @Override
    public List<CustomerResponse> getExpiringMembers(int days, Long requesterId) {
        Users requester = getRequesterOrThrow(requesterId);
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate cutoff = today.plusDays(days);
        return getVisibleCustomersForRequester(requester).stream()
                .filter(fu -> fu.getMembershipExpiry() != null
                        && !fu.getMembershipExpiry().isBefore(today)
                        && !fu.getMembershipExpiry().isAfter(cutoff))
                .sorted(java.util.Comparator.comparing(FitnessUser::getMembershipExpiry))
                .map(this::mapCustomerToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AtRiskMemberResponse> getAtRiskMembers(Long requesterId) {
        Users requester = getRequesterOrThrow(requesterId);
        List<FitnessUser> customers = getVisibleCustomersForRequester(requester);
        List<AtRiskMemberResponse> atRiskMembers = new java.util.ArrayList<>();
        java.time.LocalDate today = java.time.LocalDate.now();

        for (FitnessUser fu : customers) {
            List<String> reasons = new java.util.ArrayList<>();

            // 1. Not visited in 10 days
            java.util.Optional<Attendance> lastAttendance = attendanceRepository.findFirstByUserOrderByCheckInTimeDesc(fu.getAccount());
            if (lastAttendance.isEmpty()) {
                 reasons.add("Never visited");
            } else {
                 java.time.LocalDate lastVisitDate = lastAttendance.get().getCheckInTime().toLocalDate();
                 if (java.time.temporal.ChronoUnit.DAYS.between(lastVisitDate, today) >= 10) {
                     reasons.add("Not visited in 10 days");
                 }
            }

            // 2. Workout completion below 30%
            // Count total schedules and completed schedules for the user
            List<UserWorkoutSchedule> schedules = userWorkoutScheduleRepository.findByUser_IdOrderByStartDateTimeAsc(fu.getAccount().getId());
            if (!schedules.isEmpty()) {
                long completed = schedules.stream()
                        .filter(s -> "COMPLETED".equalsIgnoreCase(s.getCompletionStatus()))
                        .count();
                double completionRate = (double) completed / schedules.size() * 100;
                if (completionRate < 30.0) {
                    reasons.add(String.format("Workout completion below 30%% (%.0f%%)", completionRate));
                }
            }

            // 3. Membership ending soon (within 15 days)
            if (fu.getMembershipExpiry() != null) {
                 if (fu.getMembershipExpiry().isBefore(today)) {
                     reasons.add("Membership expired");
                 } else if (java.time.temporal.ChronoUnit.DAYS.between(today, fu.getMembershipExpiry()) <= 15) {
                     reasons.add("Membership ending soon");
                 }
            }

            AtRiskMemberResponse response = new AtRiskMemberResponse(
                    fu.getAccount().getId(),
                    fu.getFirstName(),
                    fu.getLastName(),
                    fu.getAccount().getEmail(),
                    fu.getPhone(),
                    reasons
            );
            atRiskMembers.add(response);
        }
        return atRiskMembers;
    }

    @Override
    public void engageAtRiskMember(Long customerId, Long requesterId, String message) {
        Users requester = getRequesterOrThrow(requesterId);
        FitnessUser customer = fitnessUserRepository.findByAccount_Id(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        // Simulate sending WhatsApp message
        log.info("Manual WhatsApp from {} to {}: {}",
                requester.getEmail(),
                customer.getPhone() != null ? customer.getPhone() : customer.getAccount().getEmail(),
                message);

        // Optionally save a notification or log history in database (simulated here)
    }

    public java.util.List<TrainerPerformanceResponse> getTrainerPerformances(Long requesterId) {
        Users requester = getRequesterOrThrow(requesterId);
        java.time.LocalDate thirtyDaysAgo = java.time.LocalDate.now().minusDays(30);

        // Get visible trainers for the requester
        java.util.List<com.fitnexus.backend.entity.Trainer> trainers;
        switch (requester.getRole()) {
            case SUPER_ADMIN -> trainers = trainerRepository.findAll();
            case ADMIN -> trainers = trainerRepository.findByAccount_Admin_Id(requester.getId());
            default -> trainers = java.util.List.of();
        }

        java.util.List<TrainerPerformanceResponse> result = new java.util.ArrayList<>();

        for (com.fitnexus.backend.entity.Trainer trainer : trainers) {
            // Assigned members
            java.util.List<com.fitnexus.backend.entity.FitnessUser> assignedUsers =
                    fitnessUserRepository.findByAssignedTrainer_Account_Id(trainer.getAccount().getId());

            int totalAssigned = assignedUsers.size();

            if (totalAssigned == 0) {
                // Still include the trainer with zero stats
                result.add(new TrainerPerformanceResponse(
                        trainer.getAccount().getId(),
                        trainer.getFirstName() + " " + (trainer.getLastName() != null ? trainer.getLastName() : ""),
                        0, 0, 0, 0.0,
                        trainer.getRating() != null ? trainer.getRating() : 0.0,
                        0, 0));
                continue;
            }

            java.util.List<Long> memberAccountIds = assignedUsers.stream()
                    .map(fu -> fu.getAccount().getId())
                    .collect(java.util.stream.Collectors.toList());

            // Attendance % — how many distinct members attended in last 30 days
            long attendedCount = attendanceRepository.countDistinctUsersByUserIdInAndDateAfter(memberAccountIds, thirtyDaysAgo);
            int attendancePct = totalAssigned > 0 ? (int) Math.round((attendedCount * 100.0) / totalAssigned) : 0;

            // Retention % — active members / total members
            long activeCount = assignedUsers.stream()
                    .filter(fu -> Boolean.TRUE.equals(fu.getAccount().getIsActive()))
                    .count();
            int retentionPct = totalAssigned > 0 ? (int) Math.round((activeCount * 100.0) / totalAssigned) : 0;

            // Revenue — all-time sum of transactions for these members
            double revenue = transactionRepository.sumAmountByMemberIdIn(memberAccountIds);

            // Transformations — distinct members with progress entries
            long transformations = progressEntryRepository.countDistinctUserByUserIdIn(memberAccountIds);

            // Rating
            double rating = trainer.getRating() != null ? trainer.getRating() : 0.0;

            // Composite score: (attendance + retention) / 2
            int score = (attendancePct + retentionPct) / 2;

            String name = trainer.getFirstName() + " " + (trainer.getLastName() != null ? trainer.getLastName() : "");

            result.add(new TrainerPerformanceResponse(
                    trainer.getAccount().getId(),
                    name.trim(),
                    totalAssigned,
                    attendancePct,
                    retentionPct,
                    revenue,
                    rating,
                    (int) transformations,
                    score
            ));
        }

        // Sort by performance score descending
        result.sort((a, b) -> Integer.compare(b.getPerformanceScore(), a.getPerformanceScore()));
        return result;
    }
}












