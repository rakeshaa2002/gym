package com.fitnexus.backend.repository;

import com.fitnexus.backend.entity.Trainer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {
    @EntityGraph(attributePaths = {"account", "account.createdBy"})
    Optional<Trainer> findById(Long id);

    @EntityGraph(attributePaths = {"account", "account.createdBy"})
    List<Trainer> findAll();
}
