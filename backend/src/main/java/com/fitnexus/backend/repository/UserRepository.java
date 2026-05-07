package com.fitnexus.backend.repository;

import com.fitnexus.backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Users> findByCreatedBy(Users createdBy);
    List<Users> findByRole(Role role);

    @Query("SELECT u FROM Users u WHERE u.isActive = true AND (:branchId IS NULL OR u.branchId = :branchId) AND u.role IN :roles ORDER BY u.name")
    List<Users> findReportingOptions(@Param("branchId") Long branchId, @Param("roles") List<Role> roles);
}

