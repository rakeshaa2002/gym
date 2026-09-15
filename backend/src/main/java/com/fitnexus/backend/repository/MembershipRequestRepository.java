package com.fitnexus.backend.repository;

import com.fitnexus.backend.entity.MembershipRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MembershipRequestRepository extends JpaRepository<MembershipRequest, Long> {
    List<MembershipRequest> findByStatusOrderByCreatedAtDesc(String status);

    List<MembershipRequest> findByMember_IdAndStatus(Long memberId, String status);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM MembershipRequest r WHERE r.member.id = :memberId")
    void deleteByMemberId(@org.springframework.data.repository.query.Param("memberId") Long memberId);
}
