package com.fitnexus.backend.service;

import com.fitnexus.backend.config.JwtService;
import com.fitnexus.backend.dto.LoginRequest;
import com.fitnexus.backend.dto.LoginResponse;
import com.fitnexus.backend.dto.RegisterRequest;
import com.fitnexus.backend.dto.RegisterResponse;
import com.fitnexus.backend.entity.FitnessUser;
import com.fitnexus.backend.entity.Role;
import com.fitnexus.backend.entity.Users;
import com.fitnexus.backend.exception.*;
import com.fitnexus.backend.repository.FitnessUserRepository;
import com.fitnexus.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImplementation implements AuthService {

    private final UserRepository userRepository;
    private final FitnessUserRepository fitnessUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;


//    Register a new fitness user (customer)
//    User will be inactive until activated by a trainer
    @Override
    public RegisterResponse register(RegisterRequest request) {
        log.info("Processing registration for email: {}", request.getEmail());

        // Validate input
        if (request.getEmail() == null || request.getEmail().isBlank() ||
                request.getPassword() == null || request.getPassword().isBlank() ||
                request.getName() == null || request.getName().isBlank()) {
            log.warn("Registration failed: Missing required fields");
            return new RegisterResponse(
                    null,
                    request.getEmail(),
                    request.getName(),
                    "All fields are required",
                    false
            );
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email already exists: {}", request.getEmail());
            throw new DuplicateResourceException("Email '" + request.getEmail() + "' is already registered");
        }

        // Validate password strength (should be done by @Pattern in DTO, but double check)
        if (request.getPassword().length() < 6) {
            log.warn("Registration failed: Password too weak for email: {}", request.getEmail());
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }

        try {
            Users account = new Users();
            account.setEmail(request.getEmail());
            account.setName(request.getName());
            account.setPassword(passwordEncoder.encode(request.getPassword()));
            account.setRole(Role.USER);
            account.setIsActive(false);
            account.setIsApproved(false);

            FitnessUser user = new FitnessUser();
            user.setAccount(account);
            user.setDetailsCompleted(false);

            FitnessUser savedUser = fitnessUserRepository.save(user);
            log.info("User registered successfully with ID: {}", savedUser.getId());

            return new RegisterResponse(
                    savedUser.getId(),
                    savedUser.getAccount().getEmail(),
                    savedUser.getAccount().getName(),
                    "Registration successful! Please wait for trainer activation",
                    true
            );
        } catch (Exception e) {
            log.error("Error during user registration: ", e);
            throw new RuntimeException("An error occurred during registration: " + e.getMessage());
        }
    }

    //Authenticate user and generate JWT token
    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("Processing login request for email: {}", request.getEmail());

        try {
            // Find user by email
            var userOptional = userRepository.findByEmail(request.getEmail());
            Optional.of(userOptional);
            if (userOptional.isEmpty()) {
                log.warn("Login failed: User not found with email: {}", request.getEmail());
                throw new UnauthorizedException("Invalid email or password");
            }

            Users foundUser = userOptional.get();

            // Verify password
            if (!passwordEncoder.matches(request.getPassword(), foundUser.getPassword())) {
                log.warn("Login failed: Invalid password for email: {}", request.getEmail());
                throw new UnauthorizedException("Invalid email or password");
            }

            // Check if user is active
            if (!foundUser.getIsActive()) {
                log.warn("Login attempted by inactive user: {}", request.getEmail());
                throw new InvalidOperationException("User account is not activated. Please contact your trainer.");
            }

            // Check if user is approved (for USER role)
            if (foundUser.getRole() == Role.USER && !foundUser.getIsApproved()) {
                log.warn("Login attempted by unapproved user: {}", request.getEmail());
                throw new InvalidOperationException("User account is not approved yet. Please wait for trainer approval.");
            }

            // Generate JWT token
            String token = jwtService.generateToken(foundUser.getEmail(), foundUser.getRole());

            // Update last login time
            foundUser.setLastLoginAt(LocalDateTime.now());
            userRepository.save(foundUser);

            log.info("User logged in successfully: {} with role: {}", request.getEmail(), foundUser.getRole());

            return new LoginResponse(
                    foundUser.getId(),
                    foundUser.getEmail(),
                    foundUser.getName(),
                    token,
                    foundUser.getRole()
            );

        } catch (UnauthorizedException | InvalidOperationException e) {
            log.warn("Authentication failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during login: ", e);
            throw new RuntimeException("An error occurred during login: " + e.getMessage());
        }
    }

//     Validate email is not already registered
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

//    Validate email format
    public boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email != null && email.matches(emailRegex);
    }
}
