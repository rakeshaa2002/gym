package com.fitnexus.backend.repository;

import com.fitnexus.backend.entity.Designation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DesignationRepository extends JpaRepository<Designation, Long> {

    // Find by department
    List<Designation> findByDepartmentId(Long departmentId);

    // Find by name
    Optional<Designation> findByName(String name);

    // Find by status
    List<Designation> findByStatus(String status);

    // Find by department and status
    List<Designation> findByDepartmentIdAndStatus(Long departmentId, String status);

    // Find by level
    List<Designation> findByLevel(String level);

    // Count designations by department
    long countByDepartmentId(Long departmentId);
}
