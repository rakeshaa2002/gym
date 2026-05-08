package com.fitnexus.backend.config;

import com.fitnexus.backend.repository.DietPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DietPlanDataCleanup implements CommandLineRunner {
    private final DietPlanRepository dietPlanRepository;

    @Override
    @Transactional
    public void run(String... args) {
        List<String> mockNames = List.of("Lean & Green", "Power Protein", "Vegan Energy Boost");
        for (String name : mockNames) {
            dietPlanRepository.deleteByNameIgnoreCase(name);
        }
    }
}
