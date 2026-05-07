package com.fitnexus.backend.repository;

import com.fitnexus.backend.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    // Find by head office
    List<Branch> findByHeadOfficeId(Long headOfficeId);

    // Find by name
    Optional<Branch> findByName(String name);

    // Find by status
    List<Branch> findByStatus(String status);

    // Find by head office and status
    List<Branch> findByHeadOfficeIdAndStatus(Long headOfficeId, String status);

    // Count branches by head office
    long countByHeadOfficeId(Long headOfficeId);
}
