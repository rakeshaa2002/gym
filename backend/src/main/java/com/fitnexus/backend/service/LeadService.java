package com.fitnexus.backend.service;

import com.fitnexus.backend.dto.LeadConvertRequest;
import com.fitnexus.backend.entity.FitnessUser;
import com.fitnexus.backend.entity.Lead;
import com.fitnexus.backend.entity.MembershipPlan;
import com.fitnexus.backend.entity.Role;
import com.fitnexus.backend.entity.Trainer;
import com.fitnexus.backend.entity.Users;
import com.fitnexus.backend.repository.FitnessUserRepository;
import com.fitnexus.backend.repository.LeadRepository;
import com.fitnexus.backend.repository.MembershipPlanRepository;
import com.fitnexus.backend.repository.TrainerRepository;
import com.fitnexus.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LeadService {
    private final LeadRepository leadRepository;
    private final UserRepository userRepository;
    private final FitnessUserRepository fitnessUserRepository;
    private final MembershipPlanRepository membershipPlanRepository;
    private final TrainerRepository trainerRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<Lead> getAllLeads() {
        return leadRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Lead getLeadById(Long id) {
        return leadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found with id: " + id));
    }

    public Lead createLead(Lead lead) {
        if (lead.getStatus() == null || lead.getStatus().isBlank()) {
            lead.setStatus("NEW");
        }
        if (lead.getSource() == null || lead.getSource().isBlank()) {
            lead.setSource("WALK_IN");
        }
        
        // Find assigned user if authenticated
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && lead.getAssignedTo() == null) {
            Users currentUser = userRepository.findByEmail(auth.getName()).orElse(null);
            lead.setAssignedTo(currentUser);
        }
        
        return leadRepository.save(lead);
    }

    public Lead updateLead(Long id, Lead leadDetails) {
        Lead lead = getLeadById(id);
        lead.setName(leadDetails.getName());
        lead.setEmail(leadDetails.getEmail());
        lead.setPhone(leadDetails.getPhone());
        lead.setStatus(leadDetails.getStatus());
        lead.setSource(leadDetails.getSource());
        lead.setGender(leadDetails.getGender());
        lead.setAge(leadDetails.getAge());
        lead.setNotes(leadDetails.getNotes());
        
        if (leadDetails.getAssignedTo() != null && leadDetails.getAssignedTo().getId() != null) {
            Users assignedUser = userRepository.findById(leadDetails.getAssignedTo().getId()).orElse(null);
            lead.setAssignedTo(assignedUser);
        }
        
        return leadRepository.save(lead);
    }

    public void deleteLead(Long id) {
        Lead lead = getLeadById(id);
        leadRepository.delete(lead);
    }

    public Users convertLeadToMember(Long id, LeadConvertRequest request) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found"));

        if ("WON".equals(lead.getStatus()) || "RENEWAL".equals(lead.getStatus())) {
            throw new IllegalArgumentException("Lead has already converted to a member.");
        }

        // Check if user account with the email already exists
        if (lead.getEmail() != null && !lead.getEmail().isBlank() && userRepository.existsByEmail(lead.getEmail())) {
            throw new IllegalArgumentException("A user with this email already exists.");
        }

        // Create Users account
        Users account = new Users();
        account.setEmail(lead.getEmail() != null && !lead.getEmail().isBlank() ? lead.getEmail() : lead.getPhone() + "@fitnexus.com");
        String rawPassword = (request.getPassword() != null && !request.getPassword().isBlank()) ? request.getPassword() : "Admin@123";
        account.setPassword(passwordEncoder.encode(rawPassword));
        account.setName(lead.getName());
        account.setRole(Role.USER);
        account.setIsActive(true);
        account.setIsApproved(true);
        
        // Find default creator / assigned trainer if applicable
        Users creator = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            creator = userRepository.findByEmail(auth.getName()).orElse(null);
        }
        account.setCreatedBy(creator);

        // Branch configuration
        if (request.getBranchId() != null) {
            account.setBranchId(request.getBranchId());
        } else if (creator != null) {
            account.setBranchId(creator.getBranchId());
            account.setHeadOfficeId(creator.getHeadOfficeId());
            account.setDepartmentId(creator.getDepartmentId());
            account.setTeamId(creator.getTeamId());
            account.setDesignationId(creator.getDesignationId());
        }

        // Hierarchy assignments
        if (creator != null) {
            account.setAdmin(creator.getAdmin() != null ? creator.getAdmin() : (creator.getRole() == Role.ADMIN ? creator : null));
            account.setManager(creator.getManager() != null ? creator.getManager() : (creator.getRole() == Role.MANAGER ? creator : null));
            account.setTrainer(creator.getTrainer() != null ? creator.getTrainer() : (creator.getRole() == Role.TRAINER ? creator : null));
        }

        account = userRepository.save(account);

        // Create FitnessUser profile
        FitnessUser fitnessUser = new FitnessUser();
        fitnessUser.setAccount(account);
        
        // Split name into first/last name
        String[] nameParts = lead.getName().split(" ", 2);
        fitnessUser.setFirstName(nameParts[0]);
        if (nameParts.length > 1) {
            fitnessUser.setLastName(nameParts[1]);
        } else {
            fitnessUser.setLastName("");
        }
        
        fitnessUser.setPhone(lead.getPhone());
        fitnessUser.setAge(lead.getAge());
        fitnessUser.setGender(lead.getGender());
        fitnessUser.setDetailsCompleted(true);
        fitnessUser.setRegistrationDate(LocalDateTime.now());
        
        // Set Plan
        String planCode = (request.getMembershipPlanCode() != null) ? request.getMembershipPlanCode() : "BASIC";
        MembershipPlan plan = membershipPlanRepository.findByCodeIgnoreCase(planCode)
                .orElseGet(() -> membershipPlanRepository.findByCodeIgnoreCase("BASIC").orElse(null));
        
        fitnessUser.setMembershipPlanRef(plan);
        fitnessUser.setMembershipPlan(plan != null ? plan.getCode() : "BASIC");
        
        if (plan != null && plan.getDurationDays() != null && plan.getDurationDays() > 0) {
            fitnessUser.setMembershipExpiry(java.time.LocalDate.now().plusDays(plan.getDurationDays()));
        }

        // Trainer assignment
        if (request.getAssignedTrainerId() != null) {
            Users trainerAccount = userRepository.findById(request.getAssignedTrainerId()).orElse(null);
            if (trainerAccount != null && trainerAccount.getRole() == Role.TRAINER) {
                Trainer trainer = trainerRepository.findByAccount_Id(request.getAssignedTrainerId()).orElse(null);
                fitnessUser.setAssignedTrainer(trainer);
                account.setTrainer(trainerAccount);
            }
        }

        fitnessUserRepository.save(fitnessUser);

        // Update Lead Status to WON
        lead.setStatus("WON");
        lead.setNotes((lead.getNotes() != null ? lead.getNotes() + "\n" : "") + "Converted to Member on " + LocalDateTime.now());
        leadRepository.save(lead);

        return account;
    }

    public String generateAiReply(Long leadId) {
        Lead lead = getLeadById(leadId);
        
        // Context variables
        String name = lead.getName().split(" ")[0];
        String goal = lead.getFitnessGoal() != null && !lead.getFitnessGoal().isBlank() ? lead.getFitnessGoal() : "reaching your fitness goals";
        String status = lead.getStatus();
        
        // Simulated Gemini AI Generation based on context
        if ("NEW".equals(status)) {
            return String.format("Hi %s,\n\nWelcome to FitNexus! We noticed you're interested in %s. Our certified trainers specialize in that area. Would you be free for a quick 10-minute intro call tomorrow to discuss how we can help you?\n\nBest,\nFitNexus Team", name, goal.toLowerCase());
        } else if ("TRIAL_BOOKED".equals(status) || "TRIAL_COMPLETED".equals(status)) {
            return String.format("Hi %s,\n\nHow was your recent trial session focused on %s? We'd love to hear your feedback and see if you're ready to start your full journey with us. We have a special discount running this week!\n\nBest,\nFitNexus Team", name, goal.toLowerCase());
        } else if ("NEGOTIATION".equals(status)) {
            return String.format("Hi %s,\n\nI understand you're thinking it over. Since your main goal is %s, I want to make sure you have the best support. I've spoken to my manager and we can waive the joining fee if you sign up by Friday. Let me know if that helps!\n\nBest,\nFitNexus Team", name, goal.toLowerCase());
        } else if ("LOST".equals(status)) {
            return String.format("Hi %s,\n\nIt's been a while! We're currently running a 'Welcome Back' promotion that's perfect for anyone looking to focus on %s. Would you like me to send over the details?\n\nBest,\nFitNexus Team", name, goal.toLowerCase());
        }
        
        return String.format("Hi %s,\n\nI noticed you were looking for guided support for %s. Would you be free for a quick chat tomorrow evening around 6:30 PM to discuss your options?\n\nBest,\nFitNexus Team", name, goal.toLowerCase());
    }
}
