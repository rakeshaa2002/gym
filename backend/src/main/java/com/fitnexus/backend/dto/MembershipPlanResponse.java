package com.fitnexus.backend.dto;

import com.fitnexus.backend.entity.MembershipPlan;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record MembershipPlanResponse(
        Long id,
        String code,
        String name,
        String description,
        Integer price,
        Integer durationDays,
        Integer maxSessionMinutes,
        boolean unlimitedAccess,
        boolean trainerChat,
        List<String> features,
        boolean active,
        String updatedAt
) {
    public static MembershipPlanResponse from(MembershipPlan p) {
        List<String> features = p.getFeatures() == null ? List.of()
                : Stream.of(p.getFeatures().split("\\r?\\n"))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
        return new MembershipPlanResponse(
                p.getId(),
                p.getCode(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getDurationDays(),
                p.getMaxSessionMinutes(),
                Boolean.TRUE.equals(p.getUnlimitedAccess()),
                Boolean.TRUE.equals(p.getTrainerChat()),
                features,
                Boolean.TRUE.equals(p.getActive()),
                p.getUpdatedAt() != null ? p.getUpdatedAt().toString() : null
        );
    }
}
