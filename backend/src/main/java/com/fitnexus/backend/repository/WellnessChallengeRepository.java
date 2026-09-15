package com.fitnexus.backend.repository;

import com.fitnexus.backend.entity.WellnessChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WellnessChallengeRepository extends JpaRepository<WellnessChallenge, Long> {
    List<WellnessChallenge> findByCreatorId(Long creatorId);
}
