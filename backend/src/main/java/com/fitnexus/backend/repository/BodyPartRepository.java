package com.fitnexus.backend.repository;

import com.fitnexus.backend.entity.BodyPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BodyPartRepository extends JpaRepository<BodyPart, Long> {
    List<BodyPart> findAllByOrderByUpdatedAtDescIdDesc();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
