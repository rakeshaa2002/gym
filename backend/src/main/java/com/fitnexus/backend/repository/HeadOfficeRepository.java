package com.fitnexus.backend.repository;

import com.fitnexus.backend.entity.HeadOffice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HeadOfficeRepository extends JpaRepository<HeadOffice, Long> {

    // Find by name
    Optional<HeadOffice> findByName(String name);

    // Find by status
    List<HeadOffice> findByStatus(String status);

    // Check if name exists
    boolean existsByName(String name);
}
