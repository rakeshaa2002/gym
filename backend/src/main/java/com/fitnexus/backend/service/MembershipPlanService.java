package com.fitnexus.backend.service;

import com.fitnexus.backend.dto.MembershipPlanRequest;
import com.fitnexus.backend.dto.MembershipPlanResponse;
import com.fitnexus.backend.entity.MembershipPlan;
import com.fitnexus.backend.exception.InvalidOperationException;
import com.fitnexus.backend.repository.MembershipPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MembershipPlanService {
    private final MembershipPlanRepository membershipPlanRepository;

    public List<MembershipPlanResponse> getAll() {
        return membershipPlanRepository.findAllByOrderByPriceAsc().stream()
                .map(MembershipPlanResponse::from).collect(Collectors.toList());
    }

    public List<MembershipPlanResponse> getActive() {
        return membershipPlanRepository.findByActiveTrueOrderByPriceAsc().stream()
                .map(MembershipPlanResponse::from).collect(Collectors.toList());
    }

    public MembershipPlanResponse getById(Long id) {
        return MembershipPlanResponse.from(find(id));
    }

    public MembershipPlanResponse create(MembershipPlanRequest request) {
        String code = normalizeCode(request.getCode());
        if (code.isBlank()) {
            throw new IllegalArgumentException("Plan code is required");
        }
        if (membershipPlanRepository.existsByCodeIgnoreCase(code)) {
            throw new IllegalArgumentException("A plan with code '" + code + "' already exists");
        }
        MembershipPlan plan = new MembershipPlan();
        plan.setCode(code);
        apply(plan, request);
        return MembershipPlanResponse.from(membershipPlanRepository.save(plan));
    }

    public MembershipPlanResponse update(Long id, MembershipPlanRequest request) {
        MembershipPlan plan = find(id);
        if (request.getCode() != null && !request.getCode().isBlank()) {
            String code = normalizeCode(request.getCode());
            if (!code.equalsIgnoreCase(plan.getCode()) && membershipPlanRepository.existsByCodeIgnoreCase(code)) {
                throw new IllegalArgumentException("A plan with code '" + code + "' already exists");
            }
            plan.setCode(code);
        }
        apply(plan, request);
        return MembershipPlanResponse.from(membershipPlanRepository.save(plan));
    }

    public void delete(Long id) {
        MembershipPlan plan = find(id);
        membershipPlanRepository.delete(plan);
    }

    private MembershipPlan find(Long id) {
        return membershipPlanRepository.findById(id)
                .orElseThrow(() -> new InvalidOperationException("Membership plan not found"));
    }

    private void apply(MembershipPlan plan, MembershipPlanRequest request) {
        if (request.getName() != null) plan.setName(request.getName().trim());
        if (plan.getName() == null || plan.getName().isBlank()) {
            throw new IllegalArgumentException("Plan name is required");
        }
        if (request.getDescription() != null) plan.setDescription(request.getDescription().trim());
        if (request.getPrice() != null) plan.setPrice(Math.max(0, request.getPrice()));
        if (request.getDurationDays() != null) plan.setDurationDays(Math.max(0, request.getDurationDays()));
        if (request.getMaxSessionMinutes() != null) plan.setMaxSessionMinutes(Math.max(0, request.getMaxSessionMinutes()));
        if (request.getUnlimitedAccess() != null) plan.setUnlimitedAccess(request.getUnlimitedAccess());
        if (request.getTrainerChat() != null) plan.setTrainerChat(request.getTrainerChat());
        if (request.getFeatures() != null) plan.setFeatures(request.getFeatures().trim());
        if (request.getActive() != null) plan.setActive(request.getActive());
    }

    private String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase().replaceAll("\\s+", "_");
    }
}
