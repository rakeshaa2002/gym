package com.fitnexus.backend.repository;

import com.fitnexus.backend.entity.Manager;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ManagerRepository extends JpaRepository<Manager, Long> {
    @EntityGraph(attributePaths = {"account", "account.createdBy"})
    Optional<Manager> findById(Long id);

    @EntityGraph(attributePaths = {"account", "account.createdBy"})
    List<Manager> findAll();
}
