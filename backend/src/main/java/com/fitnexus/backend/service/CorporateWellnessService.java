package com.fitnexus.backend.service;

import com.fitnexus.backend.dto.CorporateDashboardDto;
import com.fitnexus.backend.dto.CorporateEmployeeDto;
import com.fitnexus.backend.entity.FitnessUser;
import com.fitnexus.backend.entity.WellnessChallenge;
import com.fitnexus.backend.repository.FitnessUserRepository;
import com.fitnexus.backend.repository.WellnessChallengeRepository;
import com.fitnexus.backend.repository.ChallengeParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays;

import com.fitnexus.backend.dto.CorporateBmiDataDto;

@Service
@RequiredArgsConstructor
public class CorporateWellnessService {

    private final FitnessUserRepository fitnessUserRepository;
    private final WellnessChallengeRepository wellnessChallengeRepository;
    private final ChallengeParticipantRepository challengeParticipantRepository;

    @Transactional(readOnly = true)
    public CorporateDashboardDto getDashboardMetrics(Long hrUserId) {
        List<FitnessUser> employees = fitnessUserRepository.findByAssignedCorporateHrId(hrUserId);
        
        long totalEmployees = employees.size();
        
        // Mock active participants as 80% of employees for demo purposes if empty
        long activeParticipants = (long) (totalEmployees * 0.8);
        if (totalEmployees == 0) activeParticipants = 0;

        CorporateDashboardDto dto = new CorporateDashboardDto();
        dto.setTotalEmployees(totalEmployees);
        dto.setActiveParticipants(activeParticipants);
        dto.setWellnessScore(85L); // Mock score out of 100
        dto.setTopDepartment("Engineering");
        dto.setWellnessScoreTrend(Arrays.asList(65, 68, 74, 79, 82, 85));

        return dto;
    }

    @Transactional(readOnly = true)
    public List<CorporateEmployeeDto> getEmployees(Long hrUserId) {
        List<FitnessUser> employees = fitnessUserRepository.findByAssignedCorporateHrId(hrUserId);
        return employees.stream().map(emp -> {
            CorporateEmployeeDto dto = new CorporateEmployeeDto();
            dto.setId(emp.getId());
            dto.setName(emp.getFirstName() + " " + emp.getLastName());
            dto.setEmail(emp.getAccount().getEmail());
            dto.setDepartment(emp.getCorporateDepartment() != null ? emp.getCorporateDepartment() : "General");
            dto.setStatus("Active"); // Or logic based on membership
            dto.setLastVisit("Today"); // Or from attendance logs
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WellnessChallenge> getChallenges(Long hrUserId) {
        return wellnessChallengeRepository.findByCreatorId(hrUserId);
    }
    
    @Transactional
    public WellnessChallenge createChallenge(WellnessChallenge challenge) {
        return wellnessChallengeRepository.save(challenge);
    }

    @Transactional(readOnly = true)
    public CorporateBmiDataDto getBmiData(Long hrUserId) {
        // Build mock distribution data but use actual employee counts as a base if possible
        List<FitnessUser> employees = fitnessUserRepository.findByAssignedCorporateHrId(hrUserId);
        int count = employees.size() == 0 ? 100 : employees.size(); // Use 100 as base if empty for demo

        CorporateBmiDataDto dto = new CorporateBmiDataDto();

        CorporateBmiDataDto.CurrentBmi current = new CorporateBmiDataDto.CurrentBmi();
        current.setBmi(24.8);
        current.setCategory("Normal");
        current.setHeight(172.0);
        current.setWeight(73.0);
        current.setLastUpdated("2026-06-20");
        dto.setCurrent(current);

        // Dummy history
        CorporateBmiDataDto.BmiHistoryEntry h1 = new CorporateBmiDataDto.BmiHistoryEntry();
        h1.setDate("2026-05-20"); h1.setBmi(25.3); h1.setWeight(74.5); h1.setCategory("Overweight");
        CorporateBmiDataDto.BmiHistoryEntry h2 = new CorporateBmiDataDto.BmiHistoryEntry();
        h2.setDate("2026-06-20"); h2.setBmi(24.8); h2.setWeight(73.0); h2.setCategory("Normal");
        dto.setHistory(Arrays.asList(h1, h2));

        // Dummy distribution
        CorporateBmiDataDto.BmiDistribution d1 = new CorporateBmiDataDto.BmiDistribution();
        d1.setCategory("Normal"); d1.setCount((int)(count * 0.45)); d1.setPct(45.0);
        CorporateBmiDataDto.BmiDistribution d2 = new CorporateBmiDataDto.BmiDistribution();
        d2.setCategory("Overweight"); d2.setCount((int)(count * 0.35)); d2.setPct(35.0);
        CorporateBmiDataDto.BmiDistribution d3 = new CorporateBmiDataDto.BmiDistribution();
        d3.setCategory("Obese"); d3.setCount((int)(count * 0.15)); d3.setPct(15.0);
        CorporateBmiDataDto.BmiDistribution d4 = new CorporateBmiDataDto.BmiDistribution();
        d4.setCategory("Underweight"); d4.setCount((int)(count * 0.05)); d4.setPct(5.0);
        
        dto.setDistribution(Arrays.asList(d4, d1, d2, d3));

        return dto;
    }
}
