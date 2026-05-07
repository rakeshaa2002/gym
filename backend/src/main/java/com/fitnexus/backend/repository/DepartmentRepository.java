package com.fitnexus.backend.repository;

import com.fitnexus.backend.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    // Find by branch
    List<Department> findByBranchId(Long branchId);

    // Find by name
    Optional<Department> findByName(String name);

    // Find by status
    List<Department> findByStatus(String status);

    // Find by branch and status
    List<Department> findByBranchIdAndStatus(Long branchId, String status);

    // Count departments by branch
    long countByBranchId(Long branchId);
}
