package com.fitnexus.backend.repository;

import com.fitnexus.backend.entity.Admin;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    @EntityGraph(attributePaths = {"account", "account.createdBy"})
    Optional<Admin> findById(Long id);

    @EntityGraph(attributePaths = {"account", "account.createdBy"})
    List<Admin> findAll();
}
