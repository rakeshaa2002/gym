package com.fitnexus.backend.repository;

import com.fitnexus.backend.entity.ChallengeParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChallengeParticipantRepository extends JpaRepository<ChallengeParticipant, Long> {
    List<ChallengeParticipant> findByChallengeId(Long challengeId);
    
    @Query("SELECT COUNT(DISTINCT cp.participant.id) FROM ChallengeParticipant cp WHERE cp.challenge.creator.id = :creatorId")
    Long countDistinctParticipantsByCreatorId(Long creatorId);
}
