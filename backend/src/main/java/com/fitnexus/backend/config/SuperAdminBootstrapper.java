package com.fitnexus.backend.config;

import com.fitnexus.backend.entity.Role;
import com.fitnexus.backend.entity.SuperAdmin;
import com.fitnexus.backend.entity.Users;
import com.fitnexus.backend.repository.SuperAdminRepository;
import com.fitnexus.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SuperAdminBootstrapper implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SuperAdminRepository superAdminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${super-admin.email}")
    private String superAdminEmail;

    @Value("${super-admin.email-2:}")
    private String superAdminEmail2;

    @Value("${super-admin.password}")
    private String superAdminPassword;

    @Value("${super-admin.first-name}")
    private String superAdminFirstName;

    @Value("${super-admin.last-name:}")
    private String superAdminLastName;

    @Override
    @Transactional
    public void run(String... args) {
        createSuperAdminIfMissing(superAdminEmail);
        createSuperAdminIfMissing(superAdminEmail2);
    }

    private void createSuperAdminIfMissing(String email) {
        String normalizedEmail = normalizeOptional(email);
        if (normalizedEmail == null) {
            return;
        }

        if (userRepository.existsByEmail(normalizedEmail)) {
            log.info("Super admin account already exists for {}", normalizedEmail);
            return;
        }

        Users account = new Users();
        account.setEmail(normalizedEmail);
        account.setPassword(passwordEncoder.encode(superAdminPassword));
        account.setName(buildDisplayName(superAdminFirstName, superAdminLastName));
        account.setRole(Role.SUPER_ADMIN);
        account.setIsActive(true);
        account.setIsApproved(true);

        SuperAdmin superAdmin = new SuperAdmin();
        superAdmin.setAccount(account);
        superAdmin.setFirstName(superAdminFirstName);
        superAdmin.setLastName(normalizeOptional(superAdminLastName));

        superAdminRepository.save(superAdmin);
        log.info("Created default super admin account for {}", normalizedEmail);
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
}
