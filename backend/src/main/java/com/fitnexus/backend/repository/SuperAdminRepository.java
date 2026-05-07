package com.fitnexus.backend.repository;

import com.fitnexus.backend.entity.SuperAdmin;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SuperAdminRepository extends JpaRepository<SuperAdmin, Long> {
    @EntityGraph(attributePaths = {"account", "account.createdBy"})
    Optional<SuperAdmin> findById(Long id);

    @EntityGraph(attributePaths = {"account", "account.createdBy"})
    List<SuperAdmin> findAll();
}
