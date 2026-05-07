package com.fitnexus.backend.repository;

import com.fitnexus.backend.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    // Find by department
    List<Team> findByDepartmentId(Long departmentId);

    // Find by name
    Optional<Team> findByName(String name);

    // Find by status
    List<Team> findByStatus(String status);

    // Find by department and status
    List<Team> findByDepartmentIdAndStatus(Long departmentId, String status);

    // Count teams by department
    long countByDepartmentId(Long departmentId);
}
