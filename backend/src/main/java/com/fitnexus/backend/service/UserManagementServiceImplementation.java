package com.fitnexus.backend.service;

import com.fitnexus.backend.dto.*;
import com.fitnexus.backend.entity.*;
import com.fitnexus.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
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
public class UserManagementServiceImplementation implements UserManagementService {

    private final UserRepository userRepository;
    private final SuperAdminRepository superAdminRepository;
    private final AdminRepository adminRepository;
    private final ManagerRepository managerRepository;
    private final TrainerRepository trainerRepository;
    private final FitnessUserRepository fitnessUserRepository;
    private final PasswordEncoder passwordEncoder;

    private int getRoleLevel(Role role) {
        switch (role) {
            case SUPER_ADMIN:
                return 5;
            case ADMIN:
                return 4;
            case MANAGER:
                return 3;
            case TRAINER:
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
        account.setPassword(passwordEncoder.encode(request.getPassword()));
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
        ensureRequesterCanList(requesterId, Role.TRAINER, "trainers");
        return trainerRepository.findAll().stream()
                .map(this::mapTrainerToResponse)
                .collect(Collectors.toList());
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
        if (creator.getRole() == Role.TRAINER) {
            customer.setAssignedTrainer(trainerRepository.findById(trainerId)
                    .orElseThrow(() -> new RuntimeException("Trainer not found")));
        }
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
        copyCustomerFields(customer, request);
        customer.setDetailsCompleted(true);
        return mapCustomerToResponse(fitnessUserRepository.save(customer));
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
        fitnessUserRepository.delete(fitnessUserRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found")));
    }

    @Override
    public List<CustomerResponse> getAllCustomers(Long requesterId) {
        ensureRequesterCanList(requesterId, Role.USER, "customers");
        return fitnessUserRepository.findAll().stream()
                .map(this::mapCustomerToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomerResponse> getCustomersAssignedToTrainer(Long trainerId) {
        Users account = getAccount(trainerId, "Trainer");
        if (account.getRole() != Role.TRAINER) {
            throw new RuntimeException("User is not a Trainer");
        }
        Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new RuntimeException("Trainer not found"));
        return fitnessUserRepository.findByAssignedTrainer(trainer).stream()
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
        customer.setAssignedTrainer(trainer);
        return mapCustomerToResponse(fitnessUserRepository.save(customer));
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
                customer.getAssignedTrainer() != null ? customer.getAssignedTrainer().getAccount().getName() : null
        );
    }
}










