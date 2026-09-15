package com.fitnexus.backend.controller;

import com.fitnexus.backend.dto.CorporateDashboardDto;
import com.fitnexus.backend.dto.CorporateEmployeeDto;
import com.fitnexus.backend.dto.CorporateBmiDataDto;
import com.fitnexus.backend.entity.Users;
import com.fitnexus.backend.entity.WellnessChallenge;
import com.fitnexus.backend.repository.UserRepository;
import com.fitnexus.backend.service.CorporateWellnessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/corporate")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CorporateWellnessController {

    private final CorporateWellnessService corporateWellnessService;
    private final UserRepository userRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<CorporateDashboardDto> getDashboardMetrics(@RequestParam Long hrUserId) {
        return ResponseEntity.ok(corporateWellnessService.getDashboardMetrics(hrUserId));
    }

    @GetMapping("/employees")
    public ResponseEntity<List<CorporateEmployeeDto>> getEmployees(@RequestParam Long hrUserId) {
        return ResponseEntity.ok(corporateWellnessService.getEmployees(hrUserId));
    }

    @GetMapping("/bmi-data")
    public ResponseEntity<CorporateBmiDataDto> getBmiData(@RequestParam Long hrUserId) {
        return ResponseEntity.ok(corporateWellnessService.getBmiData(hrUserId));
    }

    @GetMapping("/challenges")
    public ResponseEntity<List<WellnessChallenge>> getChallenges(@RequestParam Long hrUserId) {
        return ResponseEntity.ok(corporateWellnessService.getChallenges(hrUserId));
    }

    @PostMapping("/challenges")
    public ResponseEntity<WellnessChallenge> createChallenge(@RequestParam Long hrUserId, @RequestBody WellnessChallenge challenge) {
        Users creator = userRepository.findById(hrUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        challenge.setCreator(creator);
        return ResponseEntity.ok(corporateWellnessService.createChallenge(challenge));
    }
}
