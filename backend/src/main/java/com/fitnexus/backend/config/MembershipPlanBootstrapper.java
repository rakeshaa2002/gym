package com.fitnexus.backend.config;

import com.fitnexus.backend.entity.MembershipPlan;
import com.fitnexus.backend.repository.MembershipPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Seeds the default subscription tiers on startup. Missing tiers are created;
 * existing tiers only have their feature bullets backfilled when blank (so admin
 * edits to price/name/etc. are preserved). Basic/Standard are time-restricted
 * (members get a daily window assigned on their profile); Premium is unlimited.
 */
@Component
@RequiredArgsConstructor
@Order(20)
public class MembershipPlanBootstrapper implements CommandLineRunner {

    private final MembershipPlanRepository membershipPlanRepository;

    private record PlanSeed(String code, String name, String description, int price, int durationDays,
                            int maxSessionMinutes, boolean unlimited, boolean trainerChat, String features) {}

    @Override
    @Transactional
    public void run(String... args) {
        List<PlanSeed> seeds = List.of(
                new PlanSeed("BASIC", "Basic",
                        "Single daily time slot, up to 1 hour per visit. Check in only during your assigned hour.",
                        299, 30, 60, false, false,
                        String.join("\n",
                                "Gym check-in & attendance",
                                "1 hour per visit",
                                "Track your own goals & progress",
                                "View assigned workout & diet plans")),
                new PlanSeed("STANDARD", "Standard",
                        "Wider assigned time slot — up to 2 hours per visit, with personalised plans.",
                        599, 30, 120, false, false,
                        String.join("\n",
                                "Everything in Basic",
                                "2 hours per visit",
                                "Personalised workout & diet plans",
                                "Detailed progress analytics")),
                new PlanSeed("PREMIUM", "Premium",
                        "Unlimited access — check in at any time, any day, no time limit.",
                        999, 30, 0, true, true,
                        String.join("\n",
                                "Everything in Standard",
                                "Unlimited anytime gym entry (no time slot)",
                                "No per-visit time limit",
                                "Wellness chat with personal trainers (Premium only)",
                                "Off-peak & all-branch access"))
        );
        for (PlanSeed seed : seeds) {
            MembershipPlan existing = membershipPlanRepository.findByCodeIgnoreCase(seed.code()).orElse(null);
            if (existing == null) {
                // New tier — create with all defaults.
                MembershipPlan plan = new MembershipPlan();
                plan.setCode(seed.code());
                plan.setName(seed.name());
                plan.setDescription(seed.description());
                plan.setPrice(seed.price());
                plan.setDurationDays(seed.durationDays());
                plan.setMaxSessionMinutes(seed.maxSessionMinutes());
                plan.setUnlimitedAccess(seed.unlimited());
                plan.setTrainerChat(seed.trainerChat());
                plan.setFeatures(seed.features());
                plan.setActive(true);
                membershipPlanRepository.save(plan);
            } else {
                // Existing tier — only fill in the bits seeded after it was created.
                boolean changed = false;
                if (existing.getFeatures() == null || existing.getFeatures().isBlank()) {
                    existing.setFeatures(seed.features());
                    changed = true;
                }
                if (existing.getTrainerChat() == null) {
                    existing.setTrainerChat(seed.trainerChat());
                    changed = true;
                }
                if (existing.getMaxSessionMinutes() == null) {
                    existing.setMaxSessionMinutes(seed.maxSessionMinutes());
                    changed = true;
                }
                // We don't offer free services — fix any tier that was seeded at ₹0.
                if (existing.getPrice() == null || existing.getPrice() <= 0) {
                    existing.setPrice(seed.price());
                    changed = true;
                }
                // Wellness chat is a Premium-only perk. Enforce it every startup so a
                // tier that was previously seeded/edited with chat (e.g. Standard) has
                // it revoked, and the chat feature bullet stripped from its card.
                boolean premiumTier = "PREMIUM".equalsIgnoreCase(existing.getCode())
                        || Boolean.TRUE.equals(existing.getUnlimitedAccess());
                if (!premiumTier) {
                    if (Boolean.TRUE.equals(existing.getTrainerChat())) {
                        existing.setTrainerChat(false);
                        changed = true;
                    }
                    String cleaned = stripChatFeature(existing.getFeatures());
                    if (cleaned != null && !cleaned.equals(existing.getFeatures())) {
                        existing.setFeatures(cleaned);
                        changed = true;
                    }
                }
                if (changed) {
                    membershipPlanRepository.save(existing);
                }
            }
        }
    }

    /** Drops any "wellness/trainer chat" feature bullet from a plan's feature list. */
    private String stripChatFeature(String features) {
        if (features == null || features.isBlank()) {
            return features;
        }
        StringBuilder sb = new StringBuilder();
        for (String line : features.split("\n")) {
            String l = line.toLowerCase();
            if (l.contains("wellness chat") || l.contains("trainer chat")) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(line);
        }
        return sb.toString();
    }
}
